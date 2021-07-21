package br.com.zup.pix

import javax.persistence.Embeddable

@Embeddable
class ContaCliente(
    val instituicao: String,
    val nomeDoTitular: String,
    val cpfDoTitular: String,
    val agencia: String,
    val numero: String
)

