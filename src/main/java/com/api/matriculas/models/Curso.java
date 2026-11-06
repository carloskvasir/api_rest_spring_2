package com.api.matriculas.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SoftDelete;

import java.util.ArrayList;
import java.util.List;

@Entity
@SoftDelete(columnName = "deleted")
@Getter
@Setter
@NoArgsConstructor
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do curso é obrigatório")
    @Size(min = 5, message = "O nome do curso deve ter no mínimo 5 caracteres")
    @Column(nullable = false)
    private String nome;

    @NotNull(message = "A carga horária é obrigatória")
    @Positive(message = "A carga horária deve ser maior que zero")
    @Column(nullable = false)
    private Integer cargaHoraria;

    @NotBlank(message = "A categoria é obrigatória")
    @Column(nullable = false)
    private String categoria;

    @NotNull(message = "O campo ativo é obrigatório")
    @Column(nullable = false)
    private Boolean ativo;

    @OneToMany(mappedBy = "curso")
    @Setter(AccessLevel.NONE)
    private List<Matricula> matriculas = new ArrayList<>();
}
