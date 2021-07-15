package br.com.zup.bcb

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Produces
import io.micronaut.http.client.annotation.Client

@Client("http://localhost:8082/api/v1/pix/keys")
interface BcbClient {

    @Post
    @Produces(MediaType.APPLICATION_XML)
    fun criarChavePix(@Body request: CreatePixKeyRequest): HttpResponse<CreatePixKeyResponse>
}