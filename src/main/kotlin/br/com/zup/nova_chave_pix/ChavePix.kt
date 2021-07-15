package br.com.zup.nova_chave_pix

import br.com.zup.TipoDaChave
import br.com.zup.TipoDaConta
import org.hibernate.annotations.GenericGenerator
import javax.persistence.*
import javax.validation.constraints.NotBlank

@Entity
class ChavePix(
    @NotBlank @Column(nullable = false) val identificadorDoCliente: String,
    @NotBlank @Column(nullable = false) @Enumerated(EnumType.STRING) val tipoDaChave: TipoDaChave,
    @NotBlank @Column(nullable = false) val chave: String,
    @NotBlank @Column(nullable = false) @Enumerated(EnumType.STRING) val tipoDaConta: TipoDaConta
) {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    val id: String? = null

    override fun toString(): String {
        return "ChavePix(identificadorDoCliente='$identificadorDoCliente', tipoDaChave=$tipoDaChave, chave='$chave', tipoDaConta=$tipoDaConta, id=$id)"
    }
}