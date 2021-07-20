package br.com.zup.pix.registrar

import br.com.zup.TipoDaChave
import br.com.zup.TipoDaConta
import br.com.zup.bcb.CreatePixKeyResponse
import br.com.zup.pix.*
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValidPixKey
@Introspected
data class NovaChavePix(
    @field:ValidUUID
    @field:NotBlank
    val clienteId: String,
    @field:NotNull
    val tipoDaChave: br.com.zup.pix.TipoDaChave?,
    @field:Size(max = 77)
    val chave: String,
    @field:NotNull
    val tipoDaConta: TipoDaConta?
) {
    fun toModel(conta: ContaCliente, chaveBCB: String): ChavePix {
        return ChavePix(
            clienteId,
            tipoDaChave = TipoDaChave.valueOf(this.tipoDaChave!!.name),
            chave = if (this.tipoDaChave == br.com.zup.pix.TipoDaChave.CHAVE_ALEATORIA) chaveBCB else this.chave,
            tipoDaConta = TipoDaConta.valueOf(this.tipoDaConta!!.name),
            conta
        )
    }
}