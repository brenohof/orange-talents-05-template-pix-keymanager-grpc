package br.com.zup.pix.remover

import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
data class RemoverChavePix(
    @field:NotBlank val pixId: String,
    @field:NotBlank val clienteId: String
)
