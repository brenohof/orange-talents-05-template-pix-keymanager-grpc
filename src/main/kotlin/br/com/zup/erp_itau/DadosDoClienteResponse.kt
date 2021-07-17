package br.com.zup.erp_itau

data class DadosDoClienteResponse(
    val id: String,
    val nome: String,
    val cpf: String,
    val instituicao: InstituicaoResponse
)
