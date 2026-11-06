package com.api.matriculas.controllers;

import com.api.matriculas.exceptions.GlobalExceptionHandler;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.Instant;

/**
 * Erros recusados pelo proprio Tomcat antes de chegar ao Spring MVC (ex: URL malformada como /cursos/%zz)
 * sao encaminhados para /error. Aqui viram ProblemDetail (RFC 7807), sem a pagina HTML padrao (SDR-017).
 */
@Hidden
@RestController
public class ErroContainerController implements ErrorController {

    @RequestMapping(value = "/error", produces = MediaType.APPLICATION_PROBLEM_JSON_VALUE)
    public ResponseEntity<ProblemDetail> tratar(HttpServletRequest request) {
        Object atributo = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        HttpStatus status = atributo instanceof Integer codigo && HttpStatus.resolve(codigo) != null
                ? HttpStatus.valueOf((Integer) atributo)
                : HttpStatus.INTERNAL_SERVER_ERROR;

        ProblemDetail problema = ProblemDetail.forStatus(status);
        problema.setTitle(status.getReasonPhrase());
        problema.setType(URI.create(GlobalExceptionHandler.BASE_URI + slug(status)));
        problema.setDetail(status.is4xxClientError()
                ? "A requisição não pôde ser processada."
                : "Ocorreu um erro interno. Tente novamente mais tarde.");
        problema.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(problema);
    }

    private static String slug(HttpStatus status) {
        return status.name().toLowerCase().replace('_', '-');
    }
}
