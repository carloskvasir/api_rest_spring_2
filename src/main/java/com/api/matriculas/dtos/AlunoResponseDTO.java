package com.api.matriculas.dtos;

import com.api.matriculas.models.Aluno;

public record AlunoResponseDTO(
    Long id,
    String nome,
    String email,
    Integer idade,
    String telefone
) {
    public static AlunoResponseDTO fromEntity(Aluno aluno) {
        return new AlunoResponseDTO(
            aluno.getId(),
            aluno.getNome(),
            aluno.getEmail(),
            aluno.getIdade(),
            aluno.getTelefone()
        );
    }
}
