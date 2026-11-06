package com.api.matriculas.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tratamento centralizado de erros em ProblemDetail (RFC 7807, SDR-002).
 * Estende {@link ResponseEntityExceptionHandler} para que os erros do proprio Spring MVC
 * (rota inexistente, 405, 415, 406...) usem o mesmo formato (SDR-017).
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    public static final String BASE_URI = "https://api.matriculas.com/errors/";
    private static final URI TIPO_PADRAO = URI.create("about:blank");

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail problem = criar(HttpStatus.BAD_REQUEST, "Bad Request", "bad-request",
                "Erro de validação nos campos informados.");

        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String campo = error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName();
            errors.put(campo, error.getDefaultMessage());
        });
        problem.setProperty("invalid_params", errors);
        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail problem = criar(HttpStatus.BAD_REQUEST, "Bad Request", "bad-request",
                "Corpo da requisição inválido ou malformado.");
        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String parametro = ex instanceof MethodArgumentTypeMismatchException m ? m.getName() : ex.getPropertyName();
        ProblemDetail problem = criar(HttpStatus.BAD_REQUEST, "Bad Request", "bad-request",
                "Valor inválido para o parâmetro '" + parametro + "'.");
        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    /** Padroniza todo ProblemDetail gerado pelo Spring: acrescenta {@code timestamp} e um {@code type} do projeto. */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
            HttpStatusCode statusCode, WebRequest request) {
        ResponseEntity<Object> resposta = super.handleExceptionInternal(ex, body, headers, statusCode, request);
        if (resposta != null && resposta.getBody() instanceof ProblemDetail problem) {
            padronizar(problem);
        }
        return resposta;
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ProblemDetail handleOrdenacaoInvalida(PropertyReferenceException ex) {
        return criar(HttpStatus.BAD_REQUEST, "Bad Request", "bad-request",
                "Propriedade de ordenação inválida: '" + ex.getPropertyName() + "'.");
    }

    @ExceptionHandler(OrdenacaoInvalidaException.class)
    public ProblemDetail handleOrdenacaoNaoPermitida(OrdenacaoInvalidaException ex) {
        return criar(HttpStatus.BAD_REQUEST, "Bad Request", "bad-request", ex.getMessage());
    }

    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ProblemDetail handleUsoInvalidoDoBanco(InvalidDataAccessApiUsageException ex) {
        if (ex.getMessage() != null && ex.getMessage().contains("Page offset")) {
            return criar(HttpStatus.BAD_REQUEST, "Bad Request", "bad-request",
                    "Página fora do limite suportado.");
        }
        return handleInesperado(ex);
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

    /** Rede de seguranca: registra o erro no log e devolve uma mensagem generica, sem vazar detalhes internos. */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleInesperado(Exception ex) {
        log.error("Erro inesperado ao processar a requisição", ex);
        return criar(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "internal-error",
                "Erro interno ao processar a requisição.");
    }

    private ProblemDetail criar(HttpStatus status, String titulo, String tipo, String detalhe) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detalhe);
        problem.setTitle(titulo);
        problem.setType(URI.create(BASE_URI + tipo));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    private void padronizar(ProblemDetail problem) {
        if (TIPO_PADRAO.equals(problem.getType())) {
            HttpStatus status = HttpStatus.resolve(problem.getStatus());
            String slug = status == null ? "error" : status.name().toLowerCase().replace('_', '-');
            problem.setType(URI.create(BASE_URI + slug));
        }
        if (problem.getProperties() == null || !problem.getProperties().containsKey("timestamp")) {
            problem.setProperty("timestamp", Instant.now());
        }
    }
}
