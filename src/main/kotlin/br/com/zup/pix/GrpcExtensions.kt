package br.com.zup.pix

import br.com.zup.NovaChavePixRequestGRpc
import br.com.zup.RemoveChavePixRequestGRpc
import br.com.zup.TipoDaConta
import br.com.zup.pix.registrar.NovaChavePix
import br.com.zup.pix.remover.RemoverChavePix

fun NovaChavePixRequestGRpc.toNovaChavePix(): NovaChavePix {
    return NovaChavePix(
        clienteId,
        when (tipoDaChave) {
          br.com.zup.TipoDaChave.UNKNOWN_TIPO_CHAVE -> null
          else -> TipoDaChave.valueOf(tipoDaChave.name)
        },
        chave,
        when (tipoDaConta) {
            TipoDaConta.UNKNOWN_TIPO_CONTA -> null
            else -> tipoDaConta
        }
    )
}

fun RemoveChavePixRequestGRpc.toRemoveChavePix(): RemoverChavePix {
    return RemoverChavePix(
        pixId = pixId,
        clienteId = clienteId
    )
}