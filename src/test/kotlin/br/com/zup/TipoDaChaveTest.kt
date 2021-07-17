package br.com.zup

import br.com.zup.pix.TipoDaChave
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@MicronautTest
class TipoDaChaveTest {

    @Test
    internal fun `deve retornar false caso CPF nulo ou vazio`() {
        with(TipoDaChave.CPF) {
            assertFalse(this.valida(""))
            assertFalse(this.valida(null))
        }
    }

    @Test
    internal fun `deve retornar false caso CPF no formato incorreto`() {
        with(TipoDaChave.CPF) {
            assertFalse(this.valida("000"))
            assertFalse(this.valida("67419644011200"))
            assertFalse(this.valida("67a196aa012"))
        }
    }

    @Test
    internal fun `deve retornar false caso CPF inválido`() {
        with(TipoDaChave.CPF) {
            assertFalse(this.valida("67419644011"))
            assertFalse(this.valida("67419644013"))
        }
    }

    @Test
    internal fun `deve retornar true caso CPF valido`() {
        with(TipoDaChave.CPF) {
            assertTrue(this.valida("67419644012"))
        }
    }

    @Test
    internal fun `deve retornar false caso TELEFONE vazio ou nulo`() {
        with(TipoDaChave.TELEFONE_CELULAR) {
            assertFalse(this.valida(""))
            assertFalse(this.valida(null))
        }
    }

    @Test
    internal fun `deve retornar false caso TELEFONE no formato incorreto`() {
        with(TipoDaChave.TELEFONE_CELULAR) {
            assertFalse(this.valida("000"))
            assertFalse(this.valida("977777777"))
            assertFalse(this.valida("+55989898988"))
        }
    }

    @Test
    internal fun `deve retornar true caso TELEFONE valido`() {
        with(TipoDaChave.TELEFONE_CELULAR) {
            assertTrue(this.valida("+5534940028922"))
        }
    }

    @Test
    internal fun `deve retornar false caso EMAIL nulo ou vazio`() {
        with(TipoDaChave.EMAIL) {
            assertFalse(this.valida(""))
            assertFalse(this.valida(null))
        }
    }

    @Test
    internal fun `deve retornar false caso EMAIL no formato incorreto`() {
        with(TipoDaChave.EMAIL) {
            assertFalse(this.valida("teste@email"))
            assertFalse(this.valida("teste.email"))
            assertFalse(this.valida("teste"))
        }
    }

    @Test
    internal fun `deve retornar true caso EMAIL válido`() {
        with(TipoDaChave.EMAIL) {
            assertTrue(this.valida("teste@email.com"))
        }
    }

    @Test
    internal fun `deve retornar falso caso CHAVE ALEATORIA preenchida`() {
        with(TipoDaChave.CHAVE_ALEATORIA) {
            assertFalse(this.valida("nao deve ser preenchida"))
        }
    }

    @Test
    internal fun `deve retornar true caso CHAVE ALEATORIA nula ou vazia`() {
        with(TipoDaChave.CHAVE_ALEATORIA) {
            assertTrue(this.valida(""))
        }
    }
}