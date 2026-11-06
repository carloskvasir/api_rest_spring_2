package com.api.matriculas.dtos;

import com.api.matriculas.models.StatusMatricula;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record MatriculaRequestDTO(
    @NotNull(message = "A data da matrícula é obrigatória")
    LocalDate dataMatricula,

    @NotNull(message = "O status da matrícula é obrigatório")
    StatusMatricula status,

    @NotNull(message = "O aluno é obrigatório")
    Long alunoId,

    @NotNull(message = "O curso é obrigatório")
    Long cursoId
) {}
