package br.com.zup.erp_itau

import br.com.zup.pix.ContaCliente
import br.com.zup.TipoDaConta

data class DadosDaContaResponse(
    val tipo: TipoDaConta,
    val instituicao: InstituicaoResponse,
    val agencia: String,
    val numero: String,
    val titular: TitularResponse
) {
    fun toModel(): ContaCliente {
        return ContaCliente(
            instituicao = instituicao.nome,
            nomeDoTitular = titular.nome,
            cpfDoTiular = titular.cpf,
            agencia = agencia,
            numero = numero
        )
    }
}

data class InstituicaoResponse(val nome: String, val ispb: String)
data class TitularResponse(val id: String, val nome: String, val cpf: String)