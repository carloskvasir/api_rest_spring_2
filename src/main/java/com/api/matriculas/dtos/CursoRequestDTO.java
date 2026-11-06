package com.api.matriculas.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CursoRequestDTO(
    @NotBlank(message = "O nome do curso é obrigatório")
    @Size(min = 5, message = "O nome do curso deve ter no mínimo 5 caracteres")
    String nome,

    @NotNull(message = "A carga horária é obrigatória")
    @Positive(message = "A carga horária deve ser maior que zero")
    Integer cargaHoraria,

    @NotBlank(message = "A categoria é obrigatória")
    String categoria,

    @NotNull(message = "O campo ativo é obrigatório")
    Boolean ativo
) {}
