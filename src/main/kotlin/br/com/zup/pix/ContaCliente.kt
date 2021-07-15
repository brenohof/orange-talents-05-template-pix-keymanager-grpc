package br.com.zup.pix

import javax.persistence.Embeddable

@Embeddable
class ContaCliente(
    instituicao: String,
    nomeDoTitular: String,
    cpfDoTiular: String,
    agencia: String,
    numero: String
)

