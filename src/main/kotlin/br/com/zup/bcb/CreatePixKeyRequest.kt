package br.com.zup.bcb

data class CreatePixKeyRequest(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccountRequest,
    val owner: OwnerRequest,
)

data class BankAccountRequest(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountType
)

data class OwnerRequest(
    val type: PersonType,
    val name: String,
    val taxIdNumber: String // CPF/CNPJ
)
