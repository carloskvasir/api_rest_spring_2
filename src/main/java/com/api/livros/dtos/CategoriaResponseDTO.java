package com.api.livros.dtos;

import com.api.livros.models.Categoria;

public record CategoriaResponseDTO(
    Long id,
    String nome
) {
    public static CategoriaResponseDTO fromEntity(Categoria entity) {
        if (entity == null) return null;
        return new CategoriaResponseDTO(
            entity.getId(),
            entity.getNome()
        );
    }
}
