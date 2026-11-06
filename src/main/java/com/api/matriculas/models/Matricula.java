package com.api.matriculas.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "A data da matrícula é obrigatória")
    @Column(nullable = false)
    private LocalDate dataMatricula;

    @NotNull(message = "O status da matrícula é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusMatricula status;

    @NotNull(message = "O aluno é obrigatório")
    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Aluno aluno;

    @NotNull(message = "O curso é obrigatório")
    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Curso curso;
}
