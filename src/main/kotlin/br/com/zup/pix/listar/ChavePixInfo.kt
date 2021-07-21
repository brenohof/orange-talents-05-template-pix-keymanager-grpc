package br.com.zup.pix.listar

import br.com.zup.TipoDaChave
import br.com.zup.TipoDaConta
import br.com.zup.pix.ChavePix
import br.com.zup.pix.ContaCliente
import java.time.LocalDateTime

data class ChavePixInfo(
    val pixId: String? = null,
    val clienteId: String? = null,
    val tipoDaChave: TipoDaChave,
    val chave: String,
    val tipoDaConta: TipoDaConta,
    val conta: ContaCliente,
    val criadaEm: LocalDateTime
) {
    companion object {
        fun of(chave: ChavePix): ChavePixInfo {
            return ChavePixInfo(
                pixId = chave.id,
                clienteId = chave.clienteId,
                tipoDaChave = chave.tipoDaChave,
                chave = chave.chave,
                tipoDaConta = chave.tipoDaConta,
                conta = chave.conta,
                criadaEm = chave.criadaEm
            )
        }
    }
}
