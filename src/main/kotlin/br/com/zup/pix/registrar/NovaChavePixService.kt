package br.com.zup.pix.registrar

import br.com.zup.bcb.*
import br.com.zup.common.handlers.ChavePixExistenteException
import br.com.zup.erp_itau.DadosDaContaResponse
import br.com.zup.erp_itau.ErpItauClient
import br.com.zup.pix.ChavePix
import br.com.zup.pix.ChavePixRepository
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService(
    val repository: ChavePixRepository,
    val itauClient: ErpItauClient,
    val bcbClient: BcbClient
) {
    val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun registra(@Valid novaChave: NovaChavePix): ChavePix {
        if (repository.existsByChave(novaChave.chave))
            throw ChavePixExistenteException("Chave Pix ${novaChave.chave} existente")

        val responseItau = itauClient.consultarContaDoCliente(
            novaChave.clienteId,
            novaChave.tipoDaConta.toString()
        )
        val conta = responseItau.body()?.toModel() ?: throw IllegalStateException("Cliente não encontrado no Itau.")
        logger.info("Consulta ao ERP Itau bem sucedida: ${responseItau.body()}")

        // cadastra a chave no bcb primeiro.
        val requestBCB = createPixKeyRequest(responseItau, novaChave)

        val responseBCB = try {
            bcbClient.criarChavePix(requestBCB)
        } catch (e: HttpClientResponseException) {
            throw IllegalStateException("Chave já existente no sistema BCB.")
        }

        val chaveBCB = responseBCB.body() ?: throw IllegalStateException("Houve um problema na requisição do BCB.")
        logger.info("Chave Pix criada no BCB com sucesso: $chaveBCB")

        val chavePix = novaChave.toModel(conta, chaveBCB.key)
        repository.save(chavePix)
        logger.info("Chave Pix persistida no banco com sucesso: $chavePix")

        return chavePix
    }

    private fun createPixKeyRequest(
        responseItau: HttpResponse<DadosDaContaResponse>,
        novaChave: NovaChavePix
    ): CreatePixKeyRequest {
        val bankAccount = with(responseItau.body()!!) {
            BankAccountRequest(instituicao.ispb, agencia, numero, AccountType.from(tipo))
        }

        val owner = with(responseItau.body()!!) {
            OwnerRequest(PersonType.NATURAL_PERSON, titular.nome, titular.cpf)
        }

        val requestBCB = with(novaChave) {
            CreatePixKeyRequest(KeyType.from(tipoDaChave), chave, bankAccount, owner)
        }
        return requestBCB
    }
}