package br.com.zup

import br.com.zup.pix.nova_chave.NovaChavePix


fun NovaChavePixRequestGRpc.toNovaChavePix(): NovaChavePix {
    return NovaChavePix(
        clienteId,
        when (tipoDaChave) {
          TipoDaChave.UNKNOWN_TIPO_CHAVE -> null
          else -> br.com.zup.pix.TipoDaChave.valueOf(tipoDaChave.name)
        },
        chave,
        when (tipoDaConta) {
            TipoDaConta.UNKNOWN_TIPO_CONTA -> null
            else -> tipoDaConta
        }
    )
}