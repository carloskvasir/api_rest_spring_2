package com.api.matriculas.notificacoes;

import java.time.LocalDate;

/** Publicado pelo MatriculaService ao criar uma matricula; consumido apos o commit da transacao. */
public record MatriculaRealizadaEvent(String emailAluno, String nomeAluno, String nomeCurso, LocalDate dataMatricula) {}
