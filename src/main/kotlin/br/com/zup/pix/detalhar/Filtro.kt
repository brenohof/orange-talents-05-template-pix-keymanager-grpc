package br.com.zup.pix.detalhar

import br.com.zup.bcb.BcbClient
import br.com.zup.common.handlers.ChavePixNaoExistenteException
import br.com.zup.pix.ChavePixRepository
import br.com.zup.pix.ValidUUID
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
sealed class Filtro {
    abstract fun filtra(repository: ChavePixRepository, bcbClient: BcbClient): ChavePixInfo

    @Introspected
    data class PorPixId(
        @field:NotBlank @field:ValidUUID val clienteId: String,
        @field:NotBlank @field:ValidUUID val pixId: String
    ): Filtro() {

        override fun filtra(repository: ChavePixRepository, bcbClient: BcbClient): ChavePixInfo {
            return repository.findById(pixId)
                .filter {it.clienteId == clienteId}
                .map(ChavePixInfo.Companion::of)
                .orElseThrow { ChavePixNaoExistenteException("Chave Pix não encontrada") }
        }
    }

    @Introspected
    data class PorChave(@field:NotBlank @field:Size(max = 77) val chave: String) : Filtro() {
        override fun filtra(repository: ChavePixRepository, bcbClient: BcbClient): ChavePixInfo {
            return repository.findByChave(chave)
                .map(ChavePixInfo.Companion::of)
                .orElseGet {
                    val response = bcbClient.buscarPorChavePix(chave)
                        when (response.status) {
                        HttpStatus.OK -> response.body()?.toChavePixInfo()
                        else -> throw ChavePixNaoExistenteException("Chave Pix não encontrada")
                    }
                }
        }
    }

    @Introspected
    class Invalido() : Filtro() {
        override fun filtra(repository: ChavePixRepository, bcbClient: BcbClient): ChavePixInfo {
            throw IllegalArgumentException("Chave Pix inválida ou não informada")
        }
    }
}
