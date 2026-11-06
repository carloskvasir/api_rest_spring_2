package com.api.matriculas.repositories;

import com.api.matriculas.models.Matricula;
import com.api.matriculas.models.StatusMatricula;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    /** Traz aluno e curso no mesmo select: evita uma consulta por linha (N+1) ja que LAZY nao e suportado com @SoftDelete. */
    @Override
    @EntityGraph(attributePaths = {"aluno", "curso"})
    Page<Matricula> findAll(Pageable pageable);

    boolean existsByCursoId(Long cursoId);

    boolean existsByAlunoId(Long alunoId);

    boolean existsByAlunoIdAndCursoIdAndStatus(Long alunoId, Long cursoId, StatusMatricula status);
}
