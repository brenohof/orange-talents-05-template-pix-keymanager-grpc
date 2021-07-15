package br.com.zup.bcb

data class BankAccountDTO(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountType
)
