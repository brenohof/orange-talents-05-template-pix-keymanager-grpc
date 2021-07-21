package br.com.zup.pix.detalhar

import br.com.zup.TipoDaChave
import br.com.zup.TipoDaConta
import br.com.zup.pix.ChavePix
import br.com.zup.pix.ContaCliente
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

@MicronautTest
class ChavePixInfoTest {

    @Test
    internal fun `deve retornar uma ChavePixInfo a partir de uma ChavePix`() {
        val clienteId = UUID.randomUUID().toString()
        val chavePix = ChavePix(
            clienteId =  clienteId,
            tipoDaChave = TipoDaChave.EMAIL,
            chave = "test@email.com",
            tipoDaConta = TipoDaConta.CONTA_POUPANCA,
            conta = ContaCliente(
                instituicao = "ITAÚ UNIBANCO S.A.",
                nomeDoTitular = "Rafael M C Ponte",
                cpfDoTitular = "02467781054",
                agencia = "0001",
                numero = "291900"
            )
        )

        val chavePixInfo = ChavePixInfo.of(chavePix)

        assertNotNull(chavePixInfo)
        assertEquals(chavePixInfo.clienteId, chavePix.clienteId)
        assertEquals(chavePixInfo.chave, chavePix.chave)
        assertEquals(chavePixInfo.tipoDaChave, chavePix.tipoDaChave)
        assertEquals(chavePixInfo.tipoDaConta, chavePix.tipoDaConta)
        assertEquals(chavePixInfo.criadaEm, chavePix.criadaEm)
        assertEquals(chavePixInfo.conta, chavePix.conta)
    }
}