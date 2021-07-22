package br.com.zup.pix.listar

import br.com.zup.ListaTodasChavePixRequest
import br.com.zup.ListarTodasPixKeyGrpcServiceGrpc
import br.com.zup.TipoDaChave
import br.com.zup.TipoDaConta
import br.com.zup.erp_itau.*
import br.com.zup.pix.ChavePix
import br.com.zup.pix.ChavePixRepository
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.hamcrest.collection.IsEmptyCollection
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.util.*


@MicronautTest(transactional = false)
class ListarTodaChavePixServiceTest(
    val erpItauClient: ErpItauClient,
    val repository: ChavePixRepository,
    val grpcClient: ListarTodasPixKeyGrpcServiceGrpc.ListarTodasPixKeyGrpcServiceBlockingStub
) {
    val clienteId = "c56dfef4-7901-44fb-84e2-a2cefb157890"
    val clienteId2 = "c56dfef4-7901-44fb-84e2-a2cefb157892"
    val tipoDaConta: TipoDaConta = TipoDaConta.CONTA_CORRENTE
    val instituicao = InstituicaoResponse("ITAÚ UNIBANCO S.A.", "60701190")
    val dadosContaCliente = DadosDaContaResponse(
        tipo = tipoDaConta,
        agencia = "0001",
        numero = "291900",
        instituicao = InstituicaoResponse("ITAÚ UNIBANCO S.A.", "60701190"),
        titular = TitularResponse(clienteId, "Rafael M C Ponte", "02467781054")
    )
    val dadosDoCliente = DadosDoClienteResponse(
        instituicao = instituicao,
        id = clienteId,
        nome = "Rafael M C Ponte",
        cpf = "02467781054",
    )

    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()

        val chave1 =  ChavePix(clienteId, TipoDaChave.CHAVE_ALEATORIA,
            UUID.randomUUID().toString(), tipoDaConta, dadosContaCliente.toModel())
        val chave2 =  ChavePix(clienteId, TipoDaChave.TELEFONE_CELULAR,
            "+5534940028922", tipoDaConta, dadosContaCliente.toModel())
        val chave3 =  ChavePix(clienteId, TipoDaChave.CHAVE_ALEATORIA,
            UUID.randomUUID().toString(), tipoDaConta, dadosContaCliente.toModel())
        val chave4 =  ChavePix(clienteId2, TipoDaChave.CHAVE_ALEATORIA,
            UUID.randomUUID().toString(), tipoDaConta, dadosContaCliente.toModel())
        val chave5 =  ChavePix(clienteId2, TipoDaChave.CPF,
            "02467781054", tipoDaConta, dadosContaCliente.toModel())

        repository.saveAll(mutableListOf(chave1, chave2, chave3, chave4, chave5))

        Mockito.`when`(erpItauClient.consultarClientePeloId(clienteId))
            .thenReturn(HttpResponse.ok(dadosDoCliente))

        Mockito.`when`(erpItauClient.consultarClientePeloId(clienteId2))
            .thenReturn(HttpResponse.ok(dadosDoCliente))
    }

    @Test
    internal fun `deve listar todas as chaves de um cliente`() {
        val expected = repository.findByClienteId(clienteId).map { it.toModel() }
        val notExpected = repository.findByClienteId(clienteId2).map { it.toModel() }

        val response = grpcClient.listaTodasChavePix(ListaTodasChavePixRequest.newBuilder()
            .setClienteId(clienteId)
            .build())

        assertNotNull(response)
        assertEquals(clienteId, response.clienteId)
        assertThat(response.chavesList, hasSize(3))
        assertThat(listOf(response.chavesList), contains(expected))
        assertThat(listOf(response.chavesList), not(contains(notExpected)))
    }

    @Test
    internal fun `deve retornar uma lista vazia caso nao possua chaves cadastradas`() {
        val clienteIdSemChaves = UUID.randomUUID().toString()

        Mockito.`when`(erpItauClient.consultarClientePeloId(clienteIdSemChaves))
            .thenReturn(HttpResponse.ok(dadosDoCliente))

        val response = grpcClient.listaTodasChavePix(ListaTodasChavePixRequest.newBuilder()
            .setClienteId(clienteIdSemChaves)
            .build())

        assertNotNull(response)
        assertEquals(clienteIdSemChaves, response.clienteId)
        assertThat(response.chavesList, hasSize(0))
        assertThat(response.chavesList, IsEmptyCollection.empty())
    }

    @Test
    internal fun `nao deve listar todas as chaves caso uuid invalido`() {
        val clienteIdInvalido = "x0238"

        val error = assertThrows<StatusRuntimeException>{
            grpcClient.listaTodasChavePix(ListaTodasChavePixRequest.newBuilder()
                .setClienteId(clienteIdInvalido)
                .build())
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("listarTodas.clienteId: não é um formato válido de UUID", status.description)
        }
    }

    @Test
    internal fun `nao deve listar todas as chaves quando cliente nao existente no itau`() {
        val clienteIdInexistente = UUID.randomUUID().toString()

        Mockito.`when`(erpItauClient.consultarClientePeloId(clienteIdInexistente))
            .thenReturn(HttpResponse.notFound())

        val error = assertThrows<StatusRuntimeException>{
            grpcClient.listaTodasChavePix(ListaTodasChavePixRequest.newBuilder()
                .setClienteId(clienteIdInexistente)
                .build())
        }

        with(error) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Cliente não encontrado no itau.", status.description)
        }
    }

    @Test
    internal fun `nao deve listar todas as chaves id vazio ou nulo`() {
        val clienteIdInvalido = ""

        val error = assertThrows<StatusRuntimeException>{
            grpcClient.listaTodasChavePix(ListaTodasChavePixRequest.newBuilder()
                .setClienteId(clienteIdInvalido)
                .build())
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @MockBean(ErpItauClient::class)
    fun itauMock(): ErpItauClient {
        return Mockito.mock(ErpItauClient::class.java)
    }
}