package com.api.matriculas.exceptions;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String BASE_URI = "https://api.matriculas.com/errors/";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationExceptions(MethodArgumentNotValidException ex) {
        ProblemDetail problem = criar(HttpStatus.BAD_REQUEST, "Bad Request", "bad-request",
                "Erro de validação nos campos informados.");

        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String campo = error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName();
            errors.put(campo, error.getDefaultMessage());
        });
        problem.setProperty("invalid_params", errors);
        return problem;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleMensagemIlegivel(HttpMessageNotReadableException ex) {
        return criar(HttpStatus.BAD_REQUEST, "Bad Request", "bad-request",
                "Corpo da requisição inválido ou malformado.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTipoInvalido(MethodArgumentTypeMismatchException ex) {
        return criar(HttpStatus.BAD_REQUEST, "Bad Request", "bad-request",
                "Valor inválido para o parâmetro '" + ex.getName() + "'.");
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ProblemDetail handleOrdenacaoInvalida(PropertyReferenceException ex) {
        return criar(HttpStatus.BAD_REQUEST, "Bad Request", "bad-request",
                "Propriedade de ordenação inválida: '" + ex.getPropertyName() + "'.");
    }

    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ProblemDetail handleUsoInvalidoDoBanco(InvalidDataAccessApiUsageException ex) {
        if (ex.getMessage() != null && ex.getMessage().contains("Page offset")) {
            return criar(HttpStatus.BAD_REQUEST, "Bad Request", "bad-request",
                    "Página fora do limite suportado.");
        }
        return criar(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "internal-error",
                "Erro interno ao processar a requisição.");
    }

    @ExceptionHandler(OrdenacaoInvalidaException.class)
    public ProblemDetail handleOrdenacaoNaoPermitida(OrdenacaoInvalidaException ex) {
        return criar(HttpStatus.BAD_REQUEST, "Bad Request", "bad-request", ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFoundException(ResourceNotFoundException ex) {
        return criar(HttpStatus.NOT_FOUND, "Resource Not Found", "not-found", ex.getMessage());
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ProblemDetail handleBusinessRule(BusinessRuleException ex) {
        return criar(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", "business-rule", ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException ex) {
        return criar(HttpStatus.CONFLICT, "Conflict", "conflict", ex.getMessage());
    }

    private ProblemDetail criar(HttpStatus status, String titulo, String tipo, String detalhe) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detalhe);
        problem.setTitle(titulo);
        problem.setType(URI.create(BASE_URI + tipo));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
