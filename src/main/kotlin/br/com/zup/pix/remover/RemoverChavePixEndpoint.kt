package br.com.zup.pix.remover

import br.com.zup.PixKeyManagerGrpcServiceGrpc
import br.com.zup.RemoveChavePixRequestGRpc
import br.com.zup.RemoveChavePixResponseGRpc
import br.com.zup.RemovePixKeyGrpcServiceGrpc
import br.com.zup.common.ErrorHandler
import br.com.zup.common.handlers.ChavePixNaoExistenteException
import br.com.zup.erp_itau.ErpItauClient
import br.com.zup.pix.ChavePixRepository
import br.com.zup.pix.toRemoveChavePix
import io.grpc.Status
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@ErrorHandler
@Singleton
class RemoverChavePixEndpoint(
    val service: RemoverChavePixService
): RemovePixKeyGrpcServiceGrpc.RemovePixKeyGrpcServiceImplBase() {
    override fun removeChavePix(
        request: RemoveChavePixRequestGRpc,
        responseObserver: StreamObserver<RemoveChavePixResponseGRpc>
    ) {
        val requestChavePix = request.toRemoveChavePix()
        service.remove(requestChavePix)

        responseObserver.onNext(
            RemoveChavePixResponseGRpc.newBuilder()
                .setMessage("Chave removida.").build()
        )
        responseObserver.onCompleted()
    }
}
