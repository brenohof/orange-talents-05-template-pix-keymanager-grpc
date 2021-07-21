package br.com.zup.bcb

import br.com.zup.TipoDaChave
import br.com.zup.TipoDaConta
import br.com.zup.pix.ContaCliente
import br.com.zup.pix.detalhar.ChavePixInfo
import br.com.zup.pix.detalhar.Instituicoes
import java.time.LocalDateTime

data class PixKeyDetailsResponse(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccountResponse,
    val owner: OwnerResponse,
    val createdAt: LocalDateTime
) {
    fun toChavePixInfo(): ChavePixInfo {
        return ChavePixInfo(
            tipoDaChave = when (keyType) {
                KeyType.CPF -> TipoDaChave.CPF
                KeyType.PHONE -> TipoDaChave.TELEFONE_CELULAR
                KeyType.EMAIL -> TipoDaChave.EMAIL
                KeyType.RANDOM -> TipoDaChave.CHAVE_ALEATORIA
                else -> TipoDaChave.UNKNOWN_TIPO_CHAVE
            },
            chave = key,
            tipoDaConta = when (bankAccount.accountType) {
                AccountType.CACC -> TipoDaConta.CONTA_CORRENTE
                AccountType.SVGS ->  TipoDaConta.CONTA_POUPANCA
            },
            conta = ContaCliente(
                instituicao = Instituicoes.nome(bankAccount.participant),
                nomeDoTitular = owner.name,
                cpfDoTitular = owner.taxIdNumber,
                agencia = bankAccount.branch,
                numero = bankAccount.accountNumber
            ),
            criadaEm = createdAt
        )
    }
}