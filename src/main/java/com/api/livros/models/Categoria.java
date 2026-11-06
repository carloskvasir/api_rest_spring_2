package com.api.livros.models;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Categoria {
    private Long id;
    private String nome;
}
