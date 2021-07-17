package br.com.zup.pix.registrar

import br.com.zup.NovaChavePixRequestGRpc
import br.com.zup.PixKeyManagerGrpcServiceGrpc
import br.com.zup.TipoDaChave
import br.com.zup.TipoDaConta
import br.com.zup.erp_itau.DadosDaContaResponse
import br.com.zup.erp_itau.ErpItauClient
import br.com.zup.erp_itau.InstituicaoResponse
import br.com.zup.erp_itau.TitularResponse
import br.com.zup.pix.ChavePixRepository
import br.com.zup.pix.toNovaChavePix
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
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

    val clienteId: String = "c56dfef4-7901-44fb-84e2-a2cefb157890"
    val tipoDaConta: TipoDaConta = TipoDaConta.CONTA_CORRENTE
    val dadosContaCliente = DadosDaContaResponse(
        tipo = tipoDaConta,
        agencia = "0001",
        numero = "291900",
        instituicao = InstituicaoResponse("ITAÚ UNIBANCO S.A.", "60701190"),
        titular = TitularResponse(clienteId, "Rafael M C Ponte", "02467781054")
    )

    @BeforeEach
    internal fun setUp() {
        chavePixRepository.deleteAll()

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
    internal fun `nao deve inserir chave ja existente`() {
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
    internal fun `nao deve inserir ao nao encontrar o cliente no itau`() {
        val idInexistente = "c56dfef4-7901-44fb-84e2-a2cefb157891"
        Mockito.`when`(erpItauClient.consultarContaDoCliente(
            idInexistente,
            tipoDaConta.toString()
        )).thenReturn(HttpResponse.notFound())

        val request = NovaChavePixRequestGRpc.newBuilder()
            .setClienteId(idInexistente)
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
            assertFalse(chavePixRepository.existsByChave(request.chave))
        }
    }

    @Test
    internal fun `nao deve inserir ao passar conta do tipo UNKNOW`() {
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
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("registra.novaChave.tipoDaConta: não deve ser nulo", status.description)
            assertFalse(chavePixRepository.existsByChave(request.chave))
        }
    }

    @Test
    internal fun `nao deve inserir ao passar chave do tipo UNKNOW`() {
        val request = NovaChavePixRequestGRpc.newBuilder()
            .setClienteId(clienteId)
            .setTipoDaChave(TipoDaChave.UNKNOWN_TIPO_CHAVE)
            .setChave("67419644012")
            .setTipoDaConta(tipoDaConta)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.novaChavePix(request)
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertFalse(chavePixRepository.existsByChave(request.chave))
        }
    }

    @Test
    internal fun `nao deve inserir uma nova chave pix com cliente UUID invalido`() {
        val request = NovaChavePixRequestGRpc.newBuilder()
            .setClienteId("-x2xc")
            .setTipoDaChave(TipoDaChave.CPF)
            .setChave("67419644012")
            .setTipoDaConta(tipoDaConta)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.novaChavePix(request)
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("registra.novaChave.clienteId: não é um formato válido de UUID", status.description)
            assertFalse(chavePixRepository.existsByChave(request.chave))
        }
    }

    @MockBean(ErpItauClient::class)
    fun erpItauMock(): ErpItauClient {
        return Mockito.mock(ErpItauClient::class.java)
    }

}