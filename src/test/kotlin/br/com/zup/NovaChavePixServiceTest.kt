package br.com.zup

import br.com.zup.erp_itau.DadosDaContaResponse
import br.com.zup.erp_itau.ErpItauClient
import br.com.zup.erp_itau.InstituicaoResponse
import br.com.zup.erp_itau.TitularResponse
import br.com.zup.pix.ChavePixRepository
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.annotation.TransactionMode
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito

@MicronautTest(transactional = false)
class NovaChavePixServiceTest(
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
    internal fun `deve criar uma nova chave pix`() {
        val request = NovaChavePixRequestGRpc.newBuilder()
            .setClienteId(clienteId)
            .setTipoDaChave(TipoDaChave.CPF)
            .setChave("67419644012")
            .setTipoDaConta(tipoDaConta)
            .build()

        val response = grpcClient.novaChavePix(request)

        with(response) {
            assertNotNull(pixID)
            assertTrue(chavePixRepository.existsById(pixID))
        }
    }

    @Test
    internal fun `deve falhar quando inserir chave ja existente`() {
        val request = NovaChavePixRequestGRpc.newBuilder()
            .setClienteId(clienteId)
            .setTipoDaChave(TipoDaChave.CPF)
            .setChave("67419644012")
            .setTipoDaConta(tipoDaConta)
            .build()

        val chavePix = request.toNovaChavePix().toModel(dadosContaCliente.toModel())
        chavePixRepository.save(chavePix)

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.novaChavePix(request)
        }

        with(error) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Chave Pix ${chavePix.chave} existente", status.description)
        }
    }

    @Test
    internal fun `deve falhar ao nao encontrar o cliente no itau`() {
        Mockito.`when`(erpItauClient.consultarContaDoCliente(
            "1234",
            tipoDaConta.toString()
        )).thenReturn(HttpResponse.notFound())

        val request = NovaChavePixRequestGRpc.newBuilder()
            .setClienteId("1234")
            .setTipoDaChave(TipoDaChave.CPF)
            .setChave("67419644012")
            .setTipoDaConta(tipoDaConta)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.novaChavePix(request)
        }

        with(error) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Cliente não encontrado no Itau.", status.description)
        }
    }

    @Test
    internal fun `deve falhar ao passar conta do tipo UNKNOW`() {
        val request = NovaChavePixRequestGRpc.newBuilder()
            .setClienteId(clienteId)
            .setTipoDaChave(TipoDaChave.CPF)
            .setChave("67419644012")
            .setTipoDaConta(TipoDaConta.UNKNOWN_TIPO_CONTA)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.novaChavePix(request)
        }

        with(error) {
            Assertions.assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("registra.novaChave.tipoDaConta: não deve ser nulo", status.description)
        }
    }

    @MockBean(ErpItauClient::class)
    fun erpItauMock(): ErpItauClient {
        return Mockito.mock(ErpItauClient::class.java)
    }

}