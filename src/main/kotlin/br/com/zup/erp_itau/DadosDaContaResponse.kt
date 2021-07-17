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