package br.com.zup.pix.listar

import br.com.zup.PixKeyResponse
import br.com.zup.TipoDaChave
import br.com.zup.TipoDaConta
import com.google.protobuf.Timestamp
import io.micronaut.core.annotation.Introspected
import java.time.LocalDateTime
import java.time.ZoneId

@Introspected
data class ChavePixResponse(
    val clienteId: String,
    val id: String,
    val chave: String,
    val tipoDaConta: TipoDaConta,
    val tipoDaChave: TipoDaChave,
    val criadaEm:  LocalDateTime
) {
    fun toModel(): PixKeyResponse? {
        return PixKeyResponse.newBuilder()
            .setClienteId(clienteId)
            .setPixId(id)
            .setChave(chave)
            .setTipoDaChave(tipoDaChave)
            .setTipoDaConta(tipoDaConta)
            .setCriadaEm(criadaEm.atZone(ZoneId.of("UTC"))
                .toInstant().let {
                    Timestamp.newBuilder()
                        .setSeconds(it.epochSecond)
                        .setNanos(it.nano).build()
                }).build()
    }
}
