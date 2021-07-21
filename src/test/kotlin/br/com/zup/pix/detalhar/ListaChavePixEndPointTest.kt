package br.com.zup.pix.detalhar

import br.com.zup.ListaChavePixRequest
import br.com.zup.ListaPixKeyGrpcServiceGrpc
import br.com.zup.TipoDaChave
import br.com.zup.TipoDaConta
import br.com.zup.bcb.*
import br.com.zup.pix.ChavePix
import br.com.zup.pix.ChavePixRepository
import br.com.zup.pix.ContaCliente
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
import java.util.*

@MicronautTest(transactional = false)
class ListaChavePixEndPointTest(
    val grpcClient: ListaPixKeyGrpcServiceGrpc.ListaPixKeyGrpcServiceBlockingStub,
    val bcbClient: BcbClient,
    val repository: ChavePixRepository
) {
    val clienteId = UUID.randomUUID().toString()
    val chavePix = ChavePix(
        clienteId =  clienteId,
        tipoDaChave = TipoDaChave.TELEFONE_CELULAR,
        chave = "+5534940028922",
        tipoDaConta = TipoDaConta.CONTA_POUPANCA,
        conta = ContaCliente(
            instituicao = "ITAÚ UNIBANCO S.A.",
            nomeDoTitular = "Rafael M C Ponte",
            cpfDoTitular = "02467781054",
            agencia = "0001",
            numero = "291900"
        )
    )
    lateinit var pixId: String

    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()

        repository.save(chavePix)
        pixId = chavePix.id!!

        Mockito.`when`(bcbClient.buscarPorChavePix(chavePix.chave)).thenReturn(
            HttpResponse.ok(pixKeyDetailsResponse(chavePix, "60701190"))
        )
    }

    @Test
    internal fun `deve listar uma chave pix por pix id e cliente id`() {
        val request = listaChavePixRequest()

        val response = grpcClient.listaChavePix(request)

        assertNotNull(response)
        assertEquals(chavePix.chave, response.chave)
        assertEquals(clienteId, response.clienteId)
        assertEquals(pixId, response.pixId)
        assertEquals(chavePix.conta.agencia, response.conta.agencia)
        assertTrue(repository.existsById(response.pixId))
    }

    @Test
    internal fun `nao deve listar uma chave pix por pix id e cliente id nao existente`() {
        val pixIdInexistente = UUID.randomUUID().toString()
        val request = ListaChavePixRequest.newBuilder()
            .setPixId(
                ListaChavePixRequest.FiltroPorPixId.newBuilder()
                    .setPixId(pixIdInexistente)
                    .setClienteId(clienteId)
                    .build()
            )
            .build()
        val chaveNaoExistente = UUID.randomUUID().toString()

        Mockito.`when`(bcbClient.buscarPorChavePix(chaveNaoExistente)).thenReturn(
            HttpResponse.notFound()
        )

        val error = assertThrows<StatusRuntimeException>{
            grpcClient.listaChavePix(request)
        }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada", status.description)
        }
    }

    @Test
    internal fun `deve listar uma chave pix por chave pix`() {
        val request = ListaChavePixRequest.newBuilder()
            .setChave(chavePix.chave)
            .build()

        val response = grpcClient.listaChavePix(request)

        assertNotNull(response)
        assertEquals(chavePix.chave, response.chave)
        assertEquals(clienteId, response.clienteId)
        assertEquals(pixId, response.pixId)
        assertEquals(chavePix.conta.agencia, response.conta.agencia)
        assertTrue(repository.existsById(response.pixId))
    }

    @Test
    internal fun `deve listar uma chave pix existente no bcb`() {
        val chaveExistenteNoBcb = "test@email.com"
        val request = ListaChavePixRequest.newBuilder()
            .setChave(chaveExistenteNoBcb)
            .build()
        val chavePixBcb = ChavePix(
            clienteId =  clienteId,
            tipoDaChave = TipoDaChave.EMAIL,
            chave = chaveExistenteNoBcb,
            tipoDaConta = TipoDaConta.CONTA_POUPANCA,
            conta = ContaCliente(
                instituicao = "Banco do Brasil S.A.",
                nomeDoTitular = "Rafael M C Ponte",
                cpfDoTitular = "02467781054",
                agencia = "0001",
                numero = "291900"
            )
        )

        Mockito.`when`(bcbClient.buscarPorChavePix(chaveExistenteNoBcb)).thenReturn(
            HttpResponse.ok(pixKeyDetailsResponse(chavePixBcb, "00000000"))
        )

        val response = grpcClient.listaChavePix(request)

        assertNotNull(response)
        assertEquals(chavePixBcb.chave, response.chave)
        assertEquals("", response.clienteId)
        assertEquals("", response.pixId)
        assertEquals(chavePix.conta.agencia, response.conta.agencia)
        assertFalse(repository.existsById(response.pixId))
    }

    @Test
    internal fun `nao deve listar uma chave pix por chave pix nao existente`() {
        val chaveNaoExistente = UUID.randomUUID().toString()
        val request = ListaChavePixRequest.newBuilder()
            .setChave(chaveNaoExistente)
            .build()

        Mockito.`when`(bcbClient.buscarPorChavePix(chaveNaoExistente)).thenReturn(
            HttpResponse.notFound()
        )

        val error = assertThrows<StatusRuntimeException>{
            grpcClient.listaChavePix(request)
        }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada", status.description)
        }
    }

    @Test
    internal fun `nao deve listar uma requisicao invalida`() {
        val chaveNaoExistente = UUID.randomUUID().toString()
        val request = ListaChavePixRequest.newBuilder()
            .build()

        val error = assertThrows<StatusRuntimeException>{
            grpcClient.listaChavePix(request)
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Chave Pix inválida ou não informada", status.description)
        }
    }

    private fun listaChavePixRequest() = ListaChavePixRequest.newBuilder()
        .setPixId(
            ListaChavePixRequest.FiltroPorPixId.newBuilder()
                .setPixId(pixId)
                .setClienteId(clienteId)
                .build()
        )
        .build()

    private fun pixKeyDetailsResponse(chavePix: ChavePix, isbp: String) = PixKeyDetailsResponse(
        KeyType.from(br.com.zup.pix.TipoDaChave.valueOf(chavePix.tipoDaChave.name)),
        chavePix.chave,
        BankAccountResponse(
            participant = isbp,
            branch = chavePix.conta.agencia,
            accountNumber = chavePix.conta.numero,
            accountType = AccountType.from(chavePix.tipoDaConta)
        ),
        OwnerResponse(
            type = PersonType.NATURAL_PERSON,
            name = chavePix.conta.nomeDoTitular,
            taxIdNumber = chavePix.conta.cpfDoTitular
        ),
        createdAt = chavePix.criadaEm
    )

    @MockBean(BcbClient::class)
    fun bcbMock(): BcbClient {
        return Mockito.mock(BcbClient::class.java)
    }
}