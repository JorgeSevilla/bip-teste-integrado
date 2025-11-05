package com.example.ejb.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "BENEFICIO")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Beneficio {

    @Id
    @Column(name = "ID")
    private Long id;

    @NotBlank(message = "O nome não pode estar em branco")
    @Column(name = "NOME")
    private String nome;

    @Column(name = "DESCRICAO")
    private String descricao;

    @NotNull(message = "O valor não pode ser nulo")
    @PositiveOrZero(message = "O valor deve ser positivo ou zero")
    @Column(name = "VALOR")
    private BigDecimal valor;

    @Column(name = "ATIVO")
    private Boolean ativo;

    @Version
    @Column(name = "VERSION")
    private Long version;
}
