package com.api.livros.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequestDTO(
    @NotBlank(message = "O nome da categoria é obrigatório")
    @Size(min = 3, message = "O nome da categoria deve ter no mínimo 3 caracteres")
    String nome
) {}
