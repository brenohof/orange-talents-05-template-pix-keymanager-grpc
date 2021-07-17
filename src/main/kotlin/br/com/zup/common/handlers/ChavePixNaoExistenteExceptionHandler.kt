package br.com.zup.common.handlers

import br.com.zup.common.ExceptionHandler
import br.com.zup.common.ExceptionHandler.StatusWithDetails
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ChavePixNaoExistenteExceptionHandler : ExceptionHandler<ChavePixNaoExistenteException> {

    override fun supports(e: Exception): Boolean {
        return e is ChavePixNaoExistenteException
    }

    override fun handle(e: ChavePixNaoExistenteException): StatusWithDetails {
        return StatusWithDetails(Status.NOT_FOUND
            .withDescription(e.message)
            .withCause(e))
    }
}
class ChavePixNaoExistenteException(message: String?) : RuntimeException(message)


