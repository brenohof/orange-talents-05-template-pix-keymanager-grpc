package br.com.zup.pix.listar

import br.com.zup.erp_itau.ErpItauClient
import br.com.zup.pix.ChavePixRepository
import br.com.zup.pix.ValidUUID
import io.micronaut.validation.Validated
import javax.inject.Singleton
import javax.validation.constraints.NotBlank

@Validated
@Singleton
class ListaTodaChavePixService(
    val repository: ChavePixRepository,
    val erpItauClient: ErpItauClient
) {

    fun listarTodas(@ValidUUID @NotBlank clienteId: String): List<ChavePixResponse> {
        erpItauClient.consultarClientePeloId(clienteId)
            .body() ?: throw IllegalStateException("Cliente não encontrado no itau.")

        return repository.findByClienteId(clienteId)
    }
}