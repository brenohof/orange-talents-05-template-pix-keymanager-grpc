package br.com.zup.pix.remover

import br.com.zup.RemoveChavePixRequestGRpc
import br.com.zup.RemovePixKeyGrpcServiceGrpc
import br.com.zup.TipoDaChave
import br.com.zup.TipoDaConta
import br.com.zup.bcb.BcbClient
import br.com.zup.bcb.DeletePixKeyRequest
import br.com.zup.bcb.DeletePixKeyResponse
import br.com.zup.erp_itau.*
import br.com.zup.pix.ChavePix
import br.com.zup.pix.ChavePixRepository
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
import java.time.LocalDateTime

@MicronautTest(transactional = false)
class RemoveChavePixServiceTest(
    val repository: ChavePixRepository,
    val erpItauClient: ErpItauClient,
    val bcbClient: BcbClient,
    val gRpcClient: RemovePixKeyGrpcServiceGrpc.RemovePixKeyGrpcServiceBlockingStub
) {
    val clienteId: String = "c56dfef4-7901-44fb-84e2-a2cefb157890"
    val tipoDaConta: TipoDaConta = TipoDaConta.CONTA_CORRENTE
    val chave = "67419644012"
    val instituicao = InstituicaoResponse("ITAÚ UNIBANCO S.A.", "60701190")
    val dadosContaCliente = DadosDaContaResponse(
        tipo = tipoDaConta,
        agencia = "0001",
        numero = "291900",
        instituicao = instituicao,
        titular = TitularResponse(clienteId, "Rafael M C Ponte", "02467781054")
    )
    val dadosDoCliente = DadosDoClienteResponse(
        instituicao = instituicao,
        id = clienteId,
        nome = "Rafael M C Ponte",
        cpf = "02467781054",
    )
    lateinit var chavePix: ChavePix

    @BeforeEach
    internal fun setUp() {
        val ispb = dadosDoCliente.instituicao.ispb
        repository.deleteAll()

        chavePix = repository.save(ChavePix(
            clienteId,
            TipoDaChave.CPF,
            chave,
            tipoDaConta,
            dadosContaCliente.toModel()
        ))

        Mockito.`when`(erpItauClient.consultarClientePeloId(
            clienteId,
        )).thenReturn(HttpResponse.ok(dadosDoCliente))

        Mockito.`when`(bcbClient.removerChavePix(
            chave, DeletePixKeyRequest(chave, ispb)
        )).thenReturn(HttpResponse.ok(DeletePixKeyResponse(chave, ispb, LocalDateTime.now())))
    }

    @Test
    internal fun `deve remover uma chave pix`() {
        val request = RemoveChavePixRequestGRpc.newBuilder()
            .setClienteId(clienteId)
            .setPixId(chavePix.id)
            .build()

        val response = gRpcClient.removeChavePix(request)

        assertNotNull(response)
        assertEquals("Chave removida.", response.message)
    }

    @Test
    internal fun `nao deve remover chave com pix UUID invalido`() {
        val request = RemoveChavePixRequestGRpc.newBuilder()
            .setClienteId(clienteId)
            .setPixId("x23928")
            .build()

        val error = assertThrows<StatusRuntimeException> {
            gRpcClient.removeChavePix(request)
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("remove.request.pixId: Pix ID com formato inválido.", status.description)
            assertTrue(repository.existsById(chavePix.id!!))
        }
    }

    @Test
    internal fun `nao deve remover chave com cliente UUID invalido`() {
        val request = RemoveChavePixRequestGRpc.newBuilder()
            .setClienteId("29388s")
            .setPixId(chavePix.id)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            gRpcClient.removeChavePix(request)
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("remove.request.clienteId: Cliente ID com formato inválido", status.description)
            assertTrue(repository.existsById(chavePix.id!!))
        }
    }

    @Test
    internal fun `nao deve remover chave que nao for encontrada`() {
        val request = RemoveChavePixRequestGRpc.newBuilder()
            .setClienteId(clienteId)
            .setPixId(chavePix.id)
            .build()

        repository.deleteById(chavePix.id!!)

        val error = assertThrows<StatusRuntimeException> {
            gRpcClient.removeChavePix(request)
        }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada.", status.description)
            assertFalse(repository.existsById(chavePix.id!!))
        }
    }

    @Test
    internal fun `nao deve remover caso cliente nao exista no Itau`() {
        val idInexistente = "c56dfef4-7901-44fb-84e2-a2cefb157891"
        Mockito.`when`(erpItauClient.consultarClientePeloId(
            idInexistente,
        )).thenReturn(HttpResponse.notFound())

        val request = RemoveChavePixRequestGRpc.newBuilder()
            .setClienteId(idInexistente)
            .setPixId(chavePix.id)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            gRpcClient.removeChavePix(request)
        }

        with(error) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Cliente não encontrado no Itau.", status.description)
            assertTrue(repository.existsById(chavePix.id!!))
        }
    }

    @Test
    internal fun `nao deve remover caso cliente seja dono da chave`() {
        val clienteDiferente = "c56dfef4-7901-44fb-84e2-a2cefb157891"
        Mockito.`when`(erpItauClient.consultarClientePeloId(
            clienteDiferente,
        )).thenReturn(HttpResponse.ok(dadosDoCliente))

        val request = RemoveChavePixRequestGRpc.newBuilder()
            .setClienteId(clienteDiferente)
            .setPixId(chavePix.id)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            gRpcClient.removeChavePix(request)
        }

        with(error) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Somente cliente dono da chave pode remove-la.", status.description)
            assertTrue(repository.existsById(chavePix.id!!))
        }
    }

    @Test
    internal fun `deve falhar ao tentar remover chave Pix nao existente no BCB`() {
        Mockito.`when`(bcbClient.removerChavePix(
            chave, DeletePixKeyRequest(chave, dadosContaCliente.instituicao.ispb)
        )).thenReturn(HttpResponse.notFound())

        val request = RemoveChavePixRequestGRpc.newBuilder()
            .setClienteId(clienteId)
            .setPixId(chavePix.id)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            gRpcClient.removeChavePix(request)
        }

        with(error) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Chave pix não existe no BCB.", status.description)
            assertFalse(repository.existsById(chavePix.id!!))
        }
    }

    @MockBean(ErpItauClient::class)
    fun erpItauMock(): ErpItauClient {
        return Mockito.mock(ErpItauClient::class.java)
    }

    @MockBean(BcbClient::class)
    fun bcbMock(): BcbClient {
        return Mockito.mock(BcbClient::class.java)
    }

}