package com.api.matriculas.repositories;

import com.api.matriculas.models.Matricula;
import com.api.matriculas.models.StatusMatricula;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    boolean existsByCursoId(Long cursoId);

    boolean existsByAlunoId(Long alunoId);

    boolean existsByAlunoIdAndCursoIdAndStatus(Long alunoId, Long cursoId, StatusMatricula status);
}
