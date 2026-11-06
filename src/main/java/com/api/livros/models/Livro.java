package com.api.livros.models;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Livro {
    private Long id;
    private String titulo;
    private String autor;
    private Integer quantidadePaginas;
    private Double preco;
    private Categoria categoria;
}
