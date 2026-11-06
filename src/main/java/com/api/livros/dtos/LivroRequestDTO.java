package com.api.livros.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record LivroRequestDTO(
    @NotBlank(message = "O título é obrigatório")
    @Size(min = 3, message = "O título deve ter no mínimo 3 caracteres")
    String titulo,

    @NotBlank(message = "O autor é obrigatório")
    String autor,

    @NotNull(message = "A quantidade de páginas é obrigatória")
    @Min(value = 1, message = "A quantidade de páginas deve ter no mínimo valor 1")
    Integer quantidadePaginas,

    @NotNull(message = "O preço é obrigatório")
    @Positive(message = "O preço deve ser maior que zero")
    Double preco,

    @NotNull(message = "O id da categoria é obrigatório")
    Long categoriaId
) {}
