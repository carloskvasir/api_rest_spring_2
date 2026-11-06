package com.api.matriculas.notificacoes;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.format.DateTimeFormatter;

/** Envia a confirmacao so depois do commit (matricula rejeitada ou revertida nao gera e-mail) e fora da requisicao. */
@Component
@RequiredArgsConstructor
public class NotificacaoMatriculaListener {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final EmailNotificador notificador;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoRealizarMatricula(MatriculaRealizadaEvent evento) {
        String texto = "Olá, %s!%n%nSua matrícula no curso \"%s\" foi registrada em %s.%n"
                .formatted(evento.nomeAluno(), evento.nomeCurso(), evento.dataMatricula().format(DATA));
        notificador.enviar(evento.emailAluno(), "Matrícula confirmada: " + evento.nomeCurso(), texto);
    }
}
