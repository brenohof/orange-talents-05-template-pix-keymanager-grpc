package br.com.zup.bcb

import java.time.LocalDateTime

data class CreatePixKeyResponse(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccountDTO,
    val owner: OwnerDTO,
    val createdAt: LocalDateTime
)
