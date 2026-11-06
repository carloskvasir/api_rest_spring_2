package com.api.matriculas.dtos;

import com.api.matriculas.models.Curso;

public record CursoResponseDTO(
    Long id,
    String nome,
    Integer cargaHoraria,
    String categoria,
    Boolean ativo
) {
    public static CursoResponseDTO fromEntity(Curso curso) {
        return new CursoResponseDTO(
            curso.getId(),
            curso.getNome(),
            curso.getCargaHoraria(),
            curso.getCategoria(),
            curso.getAtivo()
        );
    }
}
