package br.com.zup.bcb

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client

@Client("\${bcb.pix.url}/api/v1/pix/keys")
interface BcbClient {

    @Post
    @Produces(MediaType.APPLICATION_XML)
    fun criarChavePix(@Body request: CreatePixKeyRequest): HttpResponse<CreatePixKeyResponse>

    @Delete("/{key}")
    @Produces(MediaType.APPLICATION_XML)
    fun removerChavePix(@PathVariable key: String, @Body request: DeletePixKeyRequest): HttpResponse<DeletePixKeyResponse>

    @Get("/{key}")
    @Consumes(MediaType.APPLICATION_XML)
    fun buscarPorChavePix(@PathVariable key: String): HttpResponse<PixKeyDetailsResponse>

}