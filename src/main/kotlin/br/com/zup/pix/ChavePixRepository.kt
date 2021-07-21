package br.com.zup.pix

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository : JpaRepository<ChavePix, String>{
    fun existsByChave(chave: String): Boolean
    fun findByChave(chave: String): Optional<ChavePix>
}