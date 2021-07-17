package br.com.zup.pix

import br.com.zup.TipoDaChave
import br.com.zup.bcb.KeyType
import org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator

enum class TipoDaChave {

    CPF {
        override fun valida(chave: String?): Boolean {
            if (chave.isNullOrBlank()) return false

            if (!chave.matches("^[0-9]{11}\$".toRegex())) return false

            return CPFValidator().run {
                initialize(null)
                isValid(chave, null)
            }
        }
    },
    TELEFONE_CELULAR {
        override fun valida(chave: String?): Boolean {
            if (chave.isNullOrBlank()) return false

            return chave.matches("^\\+[1-9][0-9][1-9]{2}9[1-9][0-9]{7}\$".toRegex())
        }
    },
    EMAIL {
        override fun valida(chave: String?): Boolean {
            if (chave.isNullOrBlank()) return false

            return chave.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+\$".toRegex())
        }
    },
    CHAVE_ALEATORIA {
        override fun valida(chave: String?) = chave.isNullOrEmpty()
    };

    abstract fun valida(chave: String?): Boolean
}