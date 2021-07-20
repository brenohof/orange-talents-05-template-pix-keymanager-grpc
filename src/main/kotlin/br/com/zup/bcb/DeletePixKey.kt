package br.com.zup.bcb

import java.time.LocalDateTime

data class DeletePixKeyRequest (
    val key: String,
    val participant: String
)

data class DeletePixKeyResponse (
    val key: String,
    val participant: String,
    val deletedAt: LocalDateTime
)

