package br.com.zup.pix.remover

import br.com.zup.pix.ValidUUID
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
data class RemoverChavePix(
    @field:NotBlank
    @field:ValidUUID(message = "Pix ID com formato inválido.")
    val pixId: String,

    @field:NotBlank
    @field:ValidUUID(message = "Cliente ID com formato inválido")
    val clienteId: String
)
