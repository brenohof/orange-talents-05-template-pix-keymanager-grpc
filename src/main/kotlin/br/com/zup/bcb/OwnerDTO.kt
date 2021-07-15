package br.com.zup.bcb

data class OwnerDTO(
    val type: PersonType,
    val name: String,
    val taxIdNumber: String // CPF/CNPJ
)
