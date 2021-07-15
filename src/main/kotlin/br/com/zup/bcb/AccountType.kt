package br.com.zup.bcb

import br.com.zup.TipoDaConta

enum class AccountType {
    CACC, SVGS;

    companion object {
        fun from(tipoDaConta: TipoDaConta): AccountType {
            if (tipoDaConta == TipoDaConta.CONTA_POUPANCA)
                return CACC
            else
                return SVGS
        }
    }
}
