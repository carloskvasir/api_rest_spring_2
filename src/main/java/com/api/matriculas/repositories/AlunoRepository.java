package com.api.matriculas.repositories;

import com.api.matriculas.models.Aluno;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    /** Busca com lock de escrita (SELECT ... FOR UPDATE) para serializar operacoes concorrentes (SDR-016). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Aluno e where e.id = :id")
    Optional<Aluno> buscarParaEscrita(@Param("id") Long id);
}
