package br.com.zup.pix.registrar

import br.com.zup.common.handlers.ChavePixExistenteException
import br.com.zup.erp_itau.ErpItauClient
import br.com.zup.pix.ChavePix
import br.com.zup.pix.ChavePixRepository
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService(
    val repository: ChavePixRepository,
    val itauClient: ErpItauClient
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

        val chavePix = novaChave.toModel(conta)
        repository.save(chavePix)
        logger.info("Chave Pix persistida no banco com sucesso: $chavePix")

        return chavePix
    }
}