package br.com.zup.pix.listar

import br.com.zup.ListaTodasChavePixRequest
import br.com.zup.ListaTodasChavePixResponse
import br.com.zup.ListarTodasPixKeyGrpcServiceGrpc
import br.com.zup.common.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@ErrorHandler
@Singleton
class ListarTodaChavePixEndpoint(
    val service: ListarTodaChavePixService
) : ListarTodasPixKeyGrpcServiceGrpc.ListarTodasPixKeyGrpcServiceImplBase() {

    override fun listaTodasChavePix(
        request: ListaTodasChavePixRequest,
        responseObserver: StreamObserver<ListaTodasChavePixResponse>
    ) {
        val chaves = service.listarTodas(request.clienteId)

        responseObserver.onNext(ListaTodasChavePixResponse.newBuilder()
            .setClienteId(request.clienteId)
            .addAllChaves(chaves.map { it.toModel() })
            .build())
        responseObserver.onCompleted()
    }
}