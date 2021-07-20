package br.com.zup.pix.remover

import br.com.zup.bcb.BcbClient
import br.com.zup.bcb.DeletePixKeyRequest
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
    val chavePixRepository: ChavePixRepository,
    val bcbClient: BcbClient
) {
    val logger = LoggerFactory.getLogger(this::class.java)

    fun remove(@Valid request: RemoverChavePix) {
        val chavePix = chavePixRepository.findById(request.pixId)
            .orElseThrow {
                ChavePixNaoExistenteException("Chave Pix não encontrada.")
            }

        val responseItau = itauClient.consultarClientePeloId(request.clienteId)
        val clienteItau = responseItau.body() ?: throw IllegalStateException("Cliente não encontrado no Itau.")
        logger.info("Consulta ao ERP Itau bem sucedida: ${clienteItau}")

        if (chavePix.clienteId != request.clienteId)
            throw IllegalStateException("Somente cliente dono da chave pode remove-la.")

        // remove chave pix no BCB
        val requestBCB = with(clienteItau) {
            DeletePixKeyRequest(chavePix.chave, instituicao.ispb)
        }

        val responseBCB = bcbClient.removerChavePix(chavePix.chave, requestBCB)
        // normaliza a situação do sistema caso exista uma chave pix aqui, mas nao no BCB
        if (responseBCB.body() == null) {
            chavePixRepository.deleteById(request.pixId)
            throw IllegalStateException("Chave pix não existe no BCB.")
        }
        val chaveBCB = responseBCB.body()
        logger.info("Chave Pix removida no BCB com sucesso: $chaveBCB")

        chavePixRepository.deleteById(request.pixId)
        logger.info("Chave Pix ${request.pixId} removida.")
    }
}