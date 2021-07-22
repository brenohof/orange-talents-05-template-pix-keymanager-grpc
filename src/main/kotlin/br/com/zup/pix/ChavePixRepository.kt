package br.com.zup.pix

import br.com.zup.pix.listar.ChavePixResponse
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository : JpaRepository<ChavePix, String>{
    fun existsByChave(chave: String): Boolean
    fun findByChave(chave: String): Optional<ChavePix>

    /**
     * O micronaut data faz a projeção da ChavePix para a ChavePixResponse,
     * os nomes dos campos devem ser os mesmos para o bind.
     */
    fun findByClienteId(clienteId: String): List<ChavePixResponse>
}