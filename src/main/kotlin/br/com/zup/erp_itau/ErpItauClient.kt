package br.com.zup.erp_itau

import br.com.zup.TipoDaConta
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("http://localhost:9091/api/v1/")
interface ErpItauClient {

    @Get("/clientes/{clienteId}")
    fun consultarClientePeloId(@PathVariable clienteId: String) : HttpResponse<DadosDoClienteResponse>

    @Get("/clientes/{clienteId}/contas")
    fun consultarContaDoCliente(@PathVariable clienteId: String, @QueryValue tipo: String) : HttpResponse<DadosDaContaResponse>
}