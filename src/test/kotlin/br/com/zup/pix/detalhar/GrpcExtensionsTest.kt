package br.com.zup.pix.detalhar

import br.com.zup.ListaChavePixRequest
import br.com.zup.ListaChavePixRequest.FiltroCase
import br.com.zup.ListaChavePixRequest.FiltroPorPixId
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import javax.validation.ConstraintViolationException
import javax.validation.Validator

@MicronautTest
class GrpcExtensionsTest(val validator: Validator) {

    val clienteId = UUID.randomUUID().toString()
    val pixId = UUID.randomUUID().toString()
    val chavePix = "+5534940028922"

    @Test
    internal fun `deve retornar um filtro do caso pix id`() {
        val request = ListaChavePixRequest.newBuilder()
            .setPixId(
                FiltroPorPixId.newBuilder()
                    .setPixId(pixId)
                    .setClienteId(clienteId)
                    .build()
            )
            .build()

        val filtro = request.toModel(validator)

        assertTrue(request.filtroCase == FiltroCase.PIX_ID)
        assertNotNull(filtro)
        assertTrue(filtro is Filtro.PorPixId)
        if (filtro is Filtro.PorPixId) {
            assertEquals(pixId, filtro.pixId)
            assertEquals(clienteId, filtro.clienteId)
        }
    }

    @Test
    internal fun `nao deve retornar um filtro caso cliente id se for invalido`() {
        val request = ListaChavePixRequest.newBuilder()
            .setPixId(
                FiltroPorPixId.newBuilder()
                    .setPixId(pixId)
                    .setClienteId("")
                    .build()
            )
            .build()

        val error = assertThrows<ConstraintViolationException> {
            request.toModel(validator)
        }

        with(error) {
            assertNotNull(error.constraintViolations)
        }
    }

    @Test
    internal fun `nao deve retornar um filtro caso pix id se for invalido`() {
        val request = ListaChavePixRequest.newBuilder()
            .setPixId(
                FiltroPorPixId.newBuilder()
                    .setPixId("")
                    .setClienteId(clienteId)
                    .build()
            )
            .build()

        val error = assertThrows<ConstraintViolationException> {
            request.toModel(validator)
        }

        with(error) {
            assertNotNull(error.constraintViolations)
        }
    }

    @Test
    internal fun `deve retornar um filtro do caso chave pix`() {
        val request = ListaChavePixRequest.newBuilder()
            .setChave(chavePix)
            .build()

        val filtro = request.toModel(validator)

        assertTrue(request.filtroCase == FiltroCase.CHAVE)
        assertNotNull(filtro)
        assertTrue(filtro is Filtro.PorChave)
        if (filtro is Filtro.PorChave) {
            assertEquals(chavePix, filtro.chave)
        }
    }

    @Test
    internal fun `nao deve retornar um filtro caso chave pix for vazio`() {
        val request = ListaChavePixRequest.newBuilder()
            .setChave("")
            .build()

        val error = assertThrows<ConstraintViolationException> {
            request.toModel(validator)
        }

        with(error) {
            assertEquals("chave: não deve estar em branco", error.message)
        }
    }

    @Test
    internal fun `nao deve retornar um filtro caso chave pix for maior que 77 caracteres`() {
        val chaveGrande = "StringLonga".repeat(77)
        val request = ListaChavePixRequest.newBuilder()
            .setChave(chaveGrande)
            .build()

        val error = assertThrows<ConstraintViolationException> {
            request.toModel(validator)
        }

        with(error) {
            assertEquals("chave: tamanho deve ser entre 0 e 77", error.message)
        }
    }

    @Test
    internal fun `deve retornar um filtro do caso invalido`() {
        val request = ListaChavePixRequest.newBuilder()
            .build()

        val filtro = request.toModel(validator)

        assertTrue(request.filtroCase == FiltroCase.FILTRO_NOT_SET)
        assertNotNull(filtro)
        assertTrue(filtro is Filtro.Invalido)
    }
}