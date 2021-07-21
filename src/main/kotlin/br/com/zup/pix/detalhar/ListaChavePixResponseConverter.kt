package br.com.zup.pix.detalhar

import br.com.zup.ListaChavePixResponse
import com.google.protobuf.Timestamp
import java.time.ZoneId

class ListaChavePixResponseConverter {

    fun convert(chaveInfo: ChavePixInfo): ListaChavePixResponse {
        return with(chaveInfo) {
            ListaChavePixResponse.newBuilder()
                .setClienteId(clienteId?.toString() ?: "") // Protobuf usa "" como default value para String
                .setPixId(pixId?.toString() ?: "") // Protobuf usa "" como default value para String
                .setTipoDaChave(tipoDaChave)
                .setChave(chave)
                .setTitular(
                    ListaChavePixResponse.Titular.newBuilder()
                        .setCpf(conta.cpfDoTitular)
                        .setNome(conta.nomeDoTitular)
                        .build()
                )
                .setConta(
                    ListaChavePixResponse.Conta.newBuilder()
                        .setTipo(tipoDaConta)
                        .setNumero(conta.numero)
                        .setAgencia(conta.agencia)
                        .setNomeDaInstitucao(conta.instituicao)
                        .build()
                )
                .setCriadaEm(criadaEm.atZone(ZoneId.of("UTC")).toInstant().let {
                    Timestamp.newBuilder()
                        .setSeconds(it.epochSecond)
                        .setNanos(it.nano)
                        .build()
                })
                .build()
        }
    }
}
