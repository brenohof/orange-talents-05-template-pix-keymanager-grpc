package br.com.zup.pix.detalhar

import br.com.zup.ListaChavePixRequest
import br.com.zup.ListaChavePixRequest.FiltroCase
import br.com.zup.ListaChavePixResponse
import br.com.zup.ListaChavePixResponse.*
import br.com.zup.ListaPixKeyGrpcServiceGrpc
import br.com.zup.bcb.BcbClient
import br.com.zup.common.ErrorHandler
import br.com.zup.common.handlers.ChavePixNaoExistenteException
import br.com.zup.pix.ChavePixRepository
import io.grpc.Status
import io.grpc.stub.StreamObserver
import javax.inject.Singleton
import javax.validation.Validator

@ErrorHandler
@Singleton
class ListarChavePixEndpoint(
    val repository: ChavePixRepository,
    val bcbClient: BcbClient,
    val validator: Validator
) : ListaPixKeyGrpcServiceGrpc.ListaPixKeyGrpcServiceImplBase() {

    override fun listaChavePix(
        request: ListaChavePixRequest,
        responseObserver: StreamObserver<ListaChavePixResponse>
    ) {
        val filtro = request.toModel(validator)
        val chaveInfo = filtro.filtra(repository, bcbClient)

        responseObserver.onNext(ListaChavePixResponseConverter().convert(chaveInfo))
        responseObserver.onCompleted()
    }
}