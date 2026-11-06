package com.api.matriculas.dtos;

import com.api.matriculas.models.StatusMatricula;
import jakarta.validation.constraints.NotNull;

public record MatriculaStatusRequestDTO(
    @NotNull(message = "O status da matrícula é obrigatório")
    StatusMatricula status
) {}
