package br.com.zup.nova_chave_pix

import br.com.zup.NovaChavePixRequestGRpc
import br.com.zup.NovaChavePixResponseGRpc
import br.com.zup.PixKeyManagerGrpcServiceGrpc
import br.com.zup.TipoDaChave
import br.com.zup.bcb.*
import br.com.zup.erp_itau.ErpItauClient
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.exceptions.HttpClientResponseException
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
class NovaChavePixEndpoint(
    val chavePixRepository: ChavePixRepository,
    val erpItauClient: ErpItauClient,
    val bcbClient: BcbClient
): PixKeyManagerGrpcServiceGrpc.PixKeyManagerGrpcServiceImplBase() {

    val logger = LoggerFactory.getLogger(NovaChavePixEndpoint::class.java)

    override fun novaChavePix(
        request: NovaChavePixRequestGRpc,
        responseObserver: StreamObserver<NovaChavePixResponseGRpc>
    ) {
        if (request.chave.length < 0 || request.chave.length > 77) {
            return Status.INVALID_ARGUMENT
                .withDescription("Tamanho da chave deve estar entre 0 e 77.")
                .asRuntimeException().let { error ->
                    responseObserver.onError(error)
                }
        }

        when (request.tipoDaChave) {
            TipoDaChave.CPF -> if (!request.chave.matches("^[0-9]{11}\$".toRegex())) {
                return Status.INVALID_ARGUMENT
                    .withDescription("CPF no formato inválido.")
                    .augmentDescription("por exemplo: 12345678901")
                    .asRuntimeException().let { error ->
                        responseObserver.onError(error)
                    }
            }
            TipoDaChave.TELEFONE_CELULAR -> if (!request.chave.matches("^\\+[1-9][0-9][1-9]{2}9[1-9]{8}\$".toRegex())) {
                return Status.INVALID_ARGUMENT
                    .withDescription("Telefone no formato inválido.")
                    .augmentDescription("por exemplo: +5585988714077")
                    .asRuntimeException().let { error ->
                        responseObserver.onError(error)
                    }
            }
            TipoDaChave.EMAIL -> if (!request.chave.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+\$".toRegex())) {
                return Status.INVALID_ARGUMENT
                    .withDescription("Email no formato inválido.")
                    .asRuntimeException().let { error ->
                        responseObserver.onError(error)
                    }
            }
            TipoDaChave.CHAVE_ALEATORIA -> if (!request.chave.isEmpty()) {
                return Status.INVALID_ARGUMENT
                    .withDescription("Chave aleátoria não deve ser informada.")
                    .asRuntimeException().let { error ->
                        responseObserver.onError(error)
                    }
            }
            else -> return Status.INVALID_ARGUMENT
                .withDescription("Tipo de chave inválido.")
                .asRuntimeException().let { error ->
                    responseObserver.onError(error)
                }
        }

        if (chavePixRepository.existsByChave(request.chave)) {
            return Status.ALREADY_EXISTS
                .withDescription("Chave já existente.")
                .asRuntimeException().let { error ->
                    responseObserver.onError(error)
                }
        }

        var responseErpItau: HttpResponse<*>? = null

        try {
           responseErpItau = erpItauClient.consultarContaDoCliente(
               request.codigoInterno,
               request.tipoDaConta.toString()
           )
        } catch (e: HttpClientResponseException) {
            return Status.INTERNAL
                .withDescription("Houve um erro inesperado.")
                .asRuntimeException().let { error ->
                    responseObserver.onError(error)
                }
        }

        if (responseErpItau.body() == null) {
            return Status.NOT_FOUND
                .withDescription("Cliente não encontrado.")
                .asRuntimeException().let { error ->
                    responseObserver.onError(error)
                }
        }

        logger.info("Consulta ao ERP Itau bem sucedida: ${responseErpItau.body()}")

        val bankAccountDTO = with(responseErpItau.body()!!) {
            BankAccountDTO(instituicao.ispb, agencia, numero, AccountType.from(request.tipoDaConta))
        }

        val ownerDTO = with(responseErpItau.body()!!) {
            OwnerDTO(PersonType.NATURAL_PERSON, titular.nome, titular.cpf)
        }

        val requestBCB = CreatePixKeyRequest(
            KeyType.from(request.tipoDaChave),
            request.chave,
            bankAccountDTO,
            ownerDTO
        )

        var responseBCB: HttpResponse<*>? = null
        try {
            responseBCB = bcbClient.criarChavePix(requestBCB)
        } catch (e: HttpClientResponseException) {
            return Status.INTERNAL
                .withDescription("Houve um erro inesperado.")
                .asRuntimeException().let { error ->
                    responseObserver.onError(error)
                }
        }

        logger.info("Requisição ao BCB bem sucedida: ${responseBCB.body()}")

        val chavePix = chavePixRepository.save(request.toChavePix(responseBCB.body().key))

        logger.info("Chave Pix persistida no banco com sucesso: $chavePix")

        responseObserver.onNext(
            NovaChavePixResponseGRpc.newBuilder()
            .setPixID(chavePix.id)
            .build())
        responseObserver.onCompleted()
    }

    private fun NovaChavePixRequestGRpc.toChavePix(key: String): ChavePix {
        return ChavePix(codigoInterno, tipoDaChave, key, tipoDaConta)
    }
}

