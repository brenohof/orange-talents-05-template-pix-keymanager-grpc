package br.com.zup.pix.listar

import br.com.zup.PixKeyResponse
import br.com.zup.TipoDaChave
import br.com.zup.TipoDaConta
import com.google.protobuf.Timestamp
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@MicronautTest
class ChavePixResponseTest {

    @Test
    internal fun `deve retornar um PixKeyResponse`() {
        val chaveResponse =  ChavePixResponse(
            clienteId = UUID.randomUUID().toString(),
            tipoDaChave = TipoDaChave.CHAVE_ALEATORIA,
            id = UUID.randomUUID().toString(),
            tipoDaConta = TipoDaConta.CONTA_POUPANCA,
            chave = UUID.randomUUID().toString(),
            criadaEm = LocalDateTime.now()
        )

        val expected = with(chaveResponse) {
            PixKeyResponse.newBuilder()
                .setPixId(id)
                .setClienteId(clienteId)
                .setTipoDaChave(tipoDaChave)
                .setTipoDaConta(tipoDaConta)
                .setChave(chave)
                .setCriadaEm(criadaEm
                    .atZone(ZoneId.of("UTC")).toInstant().let {
                        Timestamp.newBuilder()
                            .setSeconds(it.epochSecond)
                            .setNanos(it.nano)
                            .build()
                    })
                .build()
        }

        val pixKeyResponse = chaveResponse.toModel()

        assertEquals(expected, pixKeyResponse)
    }
}