package br.com.zup

import br.com.zup.erp_itau.DadosDaContaResponse
import br.com.zup.erp_itau.ErpItauClient
import br.com.zup.erp_itau.InstituicaoResponse
import br.com.zup.erp_itau.TitularResponse
import br.com.zup.pix.ChavePixRepository
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito

@MicronautTest(transactional = false)
class ValidPixKeyTest(
    val grpcClient: PixKeyManagerGrpcServiceGrpc.PixKeyManagerGrpcServiceBlockingStub,
    val chavePixRepository: ChavePixRepository,
    val erpItauClient: ErpItauClient
) {

    lateinit var clienteId: String
    lateinit var tipoDaConta: TipoDaConta
    lateinit var dadosContaCliente: DadosDaContaResponse

    @BeforeEach
    internal fun setUp() {
        chavePixRepository.deleteAll()
        clienteId = "c56dfef4-7901-44fb-84e2-a2cefb157890"
        tipoDaConta = TipoDaConta.CONTA_CORRENTE

        dadosContaCliente = DadosDaContaResponse(
            tipoDaConta,
            InstituicaoResponse(
                "ITAÚ UNIBANCO S.A.",
                "60701190"
            ),
            agencia = "0001",
            numero = "291900",
            TitularResponse(
                clienteId,
                "Rafael M C Ponte",
                "02467781054"
            )
        )

        Mockito.`when`(erpItauClient.consultarContaDoCliente(
            clienteId,
            tipoDaConta.toString()
        )).thenReturn(HttpResponse.ok(dadosContaCliente))
    }

    @Test
    internal fun `deve falhar ao passar chave do tipo CPF no formato incorreto`() {
        val request = NovaChavePixRequestGRpc.newBuilder()
            .setClienteId(clienteId)
            .setTipoDaChave(TipoDaChave.CPF)
            .setChave("12323")
            .setTipoDaConta(tipoDaConta)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.novaChavePix(request)
        }

        with(error) {
            Assertions.assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            Assertions.assertEquals("registra.novaChave: Chave Pix inválida", status.description)
        }
    }

    @Test
    internal fun `deve falhar ao passar chave do tipo TELEFONE no formato incorreto`() {
        val request = NovaChavePixRequestGRpc.newBuilder()
            .setClienteId(clienteId)
            .setTipoDaChave(TipoDaChave.TELEFONE_CELULAR)
            .setChave("3214124")
            .setTipoDaConta(tipoDaConta)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.novaChavePix(request)
        }

        with(error) {
            Assertions.assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            Assertions.assertEquals("registra.novaChave: Chave Pix inválida", status.description)
        }
    }

    @Test
    internal fun `deve falhar ao passar chave do tipo EMAIL no formato incorreto`() {
        val request = NovaChavePixRequestGRpc.newBuilder()
            .setClienteId(clienteId)
            .setTipoDaChave(TipoDaChave.EMAIL)
            .setChave("321412")
            .setTipoDaConta(tipoDaConta)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.novaChavePix(request)
        }

        with(error) {
            Assertions.assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            Assertions.assertEquals("registra.novaChave: Chave Pix inválida", status.description)
        }
    }

    @Test
    internal fun `deve falhar ao passar chave do tipo ALEATORIA no formato incorreto`() {
        val request = NovaChavePixRequestGRpc.newBuilder()
            .setClienteId(clienteId)
            .setTipoDaChave(TipoDaChave.CHAVE_ALEATORIA)
            .setChave("321412")
            .setTipoDaConta(tipoDaConta)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.novaChavePix(request)
        }

        with(error) {
            Assertions.assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            Assertions.assertEquals("registra.novaChave: Chave Pix inválida", status.description)
        }
    }

    @Test
    internal fun `deve falhar ao passar chave do tipo UNKNOW`() {
        val request = NovaChavePixRequestGRpc.newBuilder()
            .setClienteId(clienteId)
            .setTipoDaChave(TipoDaChave.UNKNOWN_TIPO_CHAVE)
            .setChave("321412")
            .setTipoDaConta(tipoDaConta)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.novaChavePix(request)
        }

        with(error) {
            Assertions.assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @MockBean(ErpItauClient::class)
    fun erpItauMock(): ErpItauClient {
        return Mockito.mock(ErpItauClient::class.java)
    }
}