package com.api.matriculas.dtos;

import com.api.matriculas.models.Matricula;

import java.time.LocalDate;

public record MatriculaResponseDTO(
    Long id,
    LocalDate dataMatricula,
    String status,
    AlunoResponseDTO aluno,
    CursoResponseDTO curso
) {
    public static MatriculaResponseDTO fromEntity(Matricula matricula) {
        return new MatriculaResponseDTO(
            matricula.getId(),
            matricula.getDataMatricula(),
            matricula.getStatus().name(),
            AlunoResponseDTO.fromEntity(matricula.getAluno()),
            CursoResponseDTO.fromEntity(matricula.getCurso())
        );
    }
}
