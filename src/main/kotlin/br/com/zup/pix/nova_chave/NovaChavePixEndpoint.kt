package br.com.zup.pix.nova_chave

import br.com.zup.*
import br.com.zup.common.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@ErrorHandler
@Singleton
class NovaChavePixEndpoint(
    private val service: NovaChavePixService
): PixKeyManagerGrpcServiceGrpc.PixKeyManagerGrpcServiceImplBase() {

    override fun novaChavePix(
        request: NovaChavePixRequestGRpc,
        responseObserver: StreamObserver<NovaChavePixResponseGRpc>
    ) {
        val novaChave = request.toNovaChavePix()
        val chaveCriada = service.registra(novaChave)

        responseObserver.onNext(
            NovaChavePixResponseGRpc.newBuilder()
            .setPixID(chaveCriada.id)
            .build())
        responseObserver.onCompleted()
    }
}



