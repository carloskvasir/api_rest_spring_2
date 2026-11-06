package com.api.livros.dtos;

import com.api.livros.models.Livro;

public record LivroResponseDTO(
    Long id,
    String titulo,
    String autor,
    Integer quantidadePaginas,
    Double preco,
    CategoriaResponseDTO categoria
) {
    public static LivroResponseDTO fromEntity(Livro livro) {
        return new LivroResponseDTO(
            livro.getId(),
            livro.getTitulo(),
            livro.getAutor(),
            livro.getQuantidadePaginas(),
            livro.getPreco(),
            CategoriaResponseDTO.fromEntity(livro.getCategoria())
        );
    }
}
