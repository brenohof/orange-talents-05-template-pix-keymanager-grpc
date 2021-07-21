package br.com.zup.pix.registrar

import br.com.zup.NovaChavePixRequestGRpc
import br.com.zup.PixKeyManagerGrpcServiceGrpc
import br.com.zup.TipoDaChave
import br.com.zup.TipoDaConta
import br.com.zup.bcb.*
import br.com.zup.erp_itau.DadosDaContaResponse
import br.com.zup.erp_itau.ErpItauClient
import br.com.zup.erp_itau.InstituicaoResponse
import br.com.zup.erp_itau.TitularResponse
import br.com.zup.pix.ChavePixRepository
import br.com.zup.pix.toNovaChavePix
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.time.LocalDateTime

@MicronautTest(transactional = false)
class NovaChavePixServiceTest(
    val grpcClient: PixKeyManagerGrpcServiceGrpc.PixKeyManagerGrpcServiceBlockingStub,
    val chavePixRepository: ChavePixRepository,
    val erpItauClient: ErpItauClient,
    val bcbClient: BcbClient
) {
    val clienteId: String = "c56dfef4-7901-44fb-84e2-a2cefb157890"
    val tipoDaConta: TipoDaConta = TipoDaConta.CONTA_CORRENTE
    val chave: String = "67419644012"
    val dadosContaCliente = DadosDaContaResponse(
        tipo = tipoDaConta,
        agencia = "0001",
        numero = "291900",
        instituicao = InstituicaoResponse("ITAÚ UNIBANCO S.A.", "60701190"),
        titular = TitularResponse(clienteId, "Rafael M C Ponte", "02467781054")
    )
    val createPixRequest = createPixKeyRequest(chave)
    val createPixResponse = createPixKeyResponse()

    @BeforeEach
    internal fun setUp() {
        chavePixRepository.deleteAll()

        Mockito.`when`(erpItauClient.consultarContaDoCliente(
            clienteId,
            tipoDaConta.toString()
        )).thenReturn(HttpResponse.ok(dadosContaCliente))

        Mockito.`when`(bcbClient.criarChavePix(
            createPixRequest
        )).thenReturn(HttpResponse.created(createPixResponse))
    }

    @Test
    internal fun `deve criar uma nova chave pix`() {
        val request = NovaChavePixRequestGRpc.newBuilder()
            .setClienteId(clienteId)
            .setTipoDaChave(TipoDaChave.CPF)
            .setChave(chave)
            .setTipoDaConta(tipoDaConta)
            .build()

        val response = grpcClient.novaChavePix(request)

        with(response) {
            assertNotNull(pixId)
            assertTrue(chavePixRepository.existsById(pixId))
        }
    }

    @Test
    internal fun `nao deve inserir chave ja existente`() {
        val request = NovaChavePixRequestGRpc.newBuilder()
            .setClienteId(clienteId)
            .setTipoDaChave(TipoDaChave.CPF)
            .setChave(chave)
            .setTipoDaConta(tipoDaConta)
            .build()

        val chavePix = request.toNovaChavePix().toModel(dadosContaCliente.toModel(), chave)
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
            .setChave(chave)
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
            .setChave(chave)
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
            .setChave(chave)
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
            .setChave(chave)
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

    @Test
    internal fun `nao deve inserir chave se ja existe no BCB`() {
        val chaveJaExistenteBCB = "97029120086"
        val createPixRequestLocal = createPixKeyRequest(chaveJaExistenteBCB)

        Mockito.`when`(bcbClient.criarChavePix(
            createPixRequestLocal
        )).thenThrow(HttpClientResponseException::class.java)

        val request = NovaChavePixRequestGRpc.newBuilder()
            .setClienteId(clienteId)
            .setTipoDaChave(TipoDaChave.CPF)
            .setChave(chaveJaExistenteBCB)
            .setTipoDaConta(tipoDaConta)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.novaChavePix(request)
        }

        with(error) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Chave já existente no sistema BCB.", status.description)
            assertFalse(chavePixRepository.existsByChave(request.chave))
        }
    }

    @Test
    internal fun `nao deve inserir chave se acontecer um problema e o BCB nao retornar resposta`() {
        val chaveLocal = "97029120086"
        val createPixRequestLocal = createPixKeyRequest(chaveLocal)

        Mockito.`when`(bcbClient.criarChavePix(
            createPixRequestLocal
        )).thenReturn(HttpResponse.notFound())

        val request = NovaChavePixRequestGRpc.newBuilder()
            .setClienteId(clienteId)
            .setTipoDaChave(TipoDaChave.CPF)
            .setChave(chaveLocal)
            .setTipoDaConta(tipoDaConta)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.novaChavePix(request)
        }

        with(error) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Houve um problema na requisição do BCB.", status.description)
            assertFalse(chavePixRepository.existsByChave(request.chave))
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

    private fun createPixKeyRequest(chave: String) = CreatePixKeyRequest(
        keyType = KeyType.CPF,
        key = chave,
        bankAccount = with(dadosContaCliente) {
            BankAccountRequest(
                participant = instituicao.ispb,
                branch = agencia,
                accountNumber = numero,
                accountType = AccountType.from(tipoDaConta)
            )
        },
        owner = with(dadosContaCliente) {
            OwnerRequest(
                type = PersonType.NATURAL_PERSON,
                name = titular.nome,
                taxIdNumber = titular.cpf
            )
        }
    )

    private fun createPixKeyResponse() = CreatePixKeyResponse(
        keyType = KeyType.CPF,
        key = chave,
        bankAccount = with(dadosContaCliente) {
            BankAccountResponse(
                participant = instituicao.ispb,
                branch = agencia,
                accountNumber = numero,
                accountType = AccountType.from(tipoDaConta)
            )
        },
        owner = with(dadosContaCliente) {
            OwnerResponse(
                type = PersonType.NATURAL_PERSON,
                name = titular.nome,
                taxIdNumber = titular.cpf
            )
        },
        createdAt = LocalDateTime.now()
    )
}