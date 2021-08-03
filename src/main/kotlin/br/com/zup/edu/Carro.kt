package br.com.zup.edu

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.validation.constraints.NotBlank

@Entity
class Carro(
    @field:NotBlank
    @Column(nullable = false)
    val modelo: String,

    @field:NotBlank
    @Column(nullable = false)
    val placa: String
){
    @Id
    @GeneratedValue
    var id: Long? = null
        private set
}