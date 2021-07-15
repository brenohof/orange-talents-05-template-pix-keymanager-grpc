package br.com.zup.erp_itau

import br.com.zup.TipoDaConta

data class DadosDaContaResponse(
    val tipo: TipoDaConta,
    val instituicao: InstituicaoResponse,
    val agencia: String,
    val numero: String,
    val titular: TitularResponse
)