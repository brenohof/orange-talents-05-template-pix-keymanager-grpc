package br.com.zup.bcb

import br.com.zup.pix.TipoDaChave

enum class KeyType {
    CPF, CNPJ, PHONE, EMAIL, RANDOM;

    companion object {
        fun from(tipoDaChave: TipoDaChave?): KeyType {
            return when (tipoDaChave) {
                TipoDaChave.CPF -> CPF
                TipoDaChave.TELEFONE_CELULAR -> PHONE
                TipoDaChave.EMAIL -> EMAIL
                TipoDaChave.CHAVE_ALEATORIA -> RANDOM
                else -> CNPJ
            }
        }
    }
}
