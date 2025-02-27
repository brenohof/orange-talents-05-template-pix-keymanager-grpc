package br.com.zup.pix.registrar

import br.com.zup.*
import br.com.zup.common.ErrorHandler
import br.com.zup.pix.toNovaChavePix
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
            .setPixId(chaveCriada.id)
            .build())
        responseObserver.onCompleted()
    }
}



