package br.com.zup.pix.remover

import br.com.zup.common.handlers.ChavePixNaoExistenteException
import br.com.zup.erp_itau.ErpItauClient
import br.com.zup.pix.ChavePixRepository
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.validation.Valid

@Validated
@Singleton
class RemoverChavePixService(
    val itauClient: ErpItauClient,
    val chavePixRepository: ChavePixRepository
) {
    val logger = LoggerFactory.getLogger(this::class.java)

    fun remove(@Valid request: RemoverChavePix) {
        val chavePix = chavePixRepository.findById(request.pixId)
            .orElseThrow {
                ChavePixNaoExistenteException("Chave Pix não encontrada.")
            }

        val responseItau = itauClient.consultarClientePeloId(request.clienteId)
        responseItau.body() ?: throw IllegalStateException("Cliente não encontrado no Itau.")
        logger.info("Consulta ao ERP Itau bem sucedida: ${responseItau.body()}")

        if (chavePix.clienteId != request.clienteId)
            throw IllegalStateException("Somente cliente dono da chave pode remove-la.")

        chavePixRepository.deleteById(request.pixId)
        logger.info("Chave Pix ${request.pixId} removida.")
    }
}