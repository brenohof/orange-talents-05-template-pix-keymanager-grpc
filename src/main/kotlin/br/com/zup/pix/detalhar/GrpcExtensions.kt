package br.com.zup.pix.detalhar

import br.com.zup.ListaChavePixRequest
import javax.validation.ConstraintViolationException
import javax.validation.Validator

fun ListaChavePixRequest.toModel(validator: Validator): Filtro {

    val filtro = when(filtroCase) {
        ListaChavePixRequest.FiltroCase.PIX_ID -> pixId.let {
            Filtro.PorPixId(it.clienteId, it.pixId)
        }
        ListaChavePixRequest.FiltroCase.CHAVE -> Filtro.PorChave(chave)
        ListaChavePixRequest.FiltroCase.FILTRO_NOT_SET -> Filtro.Invalido()
    }

    val violations = validator.validate(filtro)
    if (violations.isNotEmpty()) {
        throw ConstraintViolationException(violations)
    }

    return filtro
}