package br.com.zup.nova_chave_pix

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface ChavePixRepository : JpaRepository<ChavePix, String>{
    fun existsByChave(chave: String): Boolean
}