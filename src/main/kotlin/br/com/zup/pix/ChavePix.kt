package br.com.zup.pix

import br.com.zup.TipoDaChave
import br.com.zup.TipoDaConta
import org.hibernate.annotations.GenericGenerator
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
class ChavePix(
    @NotNull @Column(nullable = false)
    val clienteId: String,

    @NotNull @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val tipoDaChave: TipoDaChave,

    @NotBlank @Column(unique = true, nullable = false)
    val chave: String,

    @NotBlank @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val tipoDaConta: TipoDaConta,

    @field:Valid
    @Embedded
    val conta: ContaCliente
) {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    val id: String? = null

    override fun toString(): String {
        return "ChavePix(identificadorDoCliente='$clienteId', tipoDaChave=$tipoDaChave, chave='$chave', tipoDaConta=$tipoDaConta, id=$id)"
    }
}