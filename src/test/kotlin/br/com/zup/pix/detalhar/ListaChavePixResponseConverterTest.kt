package br.com.zup.pix.detalhar

import br.com.zup.TipoDaChave
import br.com.zup.TipoDaConta
import br.com.zup.pix.ContaCliente
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@MicronautTest
class ListaChavePixResponseConverterTest {

    @Test
    internal fun `deve converter um ChavePixInfo para ListaChavePixResponse`() {
        val clienteId = UUID.randomUUID().toString()
        val pixId = UUID.randomUUID().toString()
        val chavePix = ChavePixInfo(
            clienteId =  clienteId,
            pixId = pixId,
            tipoDaChave = TipoDaChave.EMAIL,
            chave = "test@email.com",
            tipoDaConta = TipoDaConta.CONTA_POUPANCA,
            conta = ContaCliente(
                instituicao = "ITAÚ UNIBANCO S.A.",
                nomeDoTitular = "Rafael M C Ponte",
                cpfDoTitular = "02467781054",
                agencia = "0001",
                numero = "291900"
            ),
            criadaEm = LocalDateTime.now()
        )

        val response = ListaChavePixResponseConverter().convert(chavePix)

        assertNotNull(response)
        assertEquals(chavePix.pixId, response.pixId)
        assertEquals(chavePix.clienteId, response.clienteId)
        assertEquals(chavePix.tipoDaChave, response.tipoDaChave)
        assertEquals(chavePix.tipoDaConta, response.conta.tipo)
        assertEquals(chavePix.chave, response.chave)
        chavePix.criadaEm.atZone(ZoneId.of("UTC")).toInstant().let {
            assertEquals(it.epochSecond , response.criadaEm.seconds)
            assertEquals(it.nano, response.criadaEm.nanos)
        }
    }
}