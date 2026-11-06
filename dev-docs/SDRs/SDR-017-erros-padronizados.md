# SDR-017: Erros padronizados em ProblemDetail e sem vazamento de detalhes

**Data:** 2026-11-05
**Status:** Aceito

## 1. Contexto e Motivação
O SDR-002 adotou a RFC 7807 (`ProblemDetail`), mas só para as exceções tratadas manualmente. Erros gerados pelo próprio Spring MVC (rota inexistente, 405, 415, 406) saíam no formato padrão do Spring, e o `spring-boot-devtools` (ativo em `spring-boot:run` e no Docker) liga `include-stacktrace`, expondo o stack trace no corpo das respostas. Achado da revisão final.

## 2. Decisão
- `GlobalExceptionHandler` passa a **estender `ResponseEntityExceptionHandler`**, que já mapeia as exceções do Spring MVC para `ProblemDetail`. Os tratamentos de validação (`invalid_params`), JSON ilegível e tipo de parâmetro viraram *overrides* dos métodos da classe base, mantendo o formato anterior.
- `handleExceptionInternal` é sobrescrito para **padronizar** qualquer `ProblemDetail`: acrescenta `timestamp` e troca o `type` padrão `about:blank` por `https://api.matriculas.com/errors/<status>` (ex.: `not-found`, `method-not-allowed`, `unsupported-media-type`, `not-acceptable`).
- Rede de segurança `@ExceptionHandler(Exception.class)`: registra o erro no log e responde **500** com mensagem genérica, sem a mensagem da exceção nem o stack trace.
- `application.properties`: `server.error.include-stacktrace=never`, `include-message=never` e `include-binding-errors=never`, sobrepondo os padrões do devtools.
- Alternativa descartada: `spring.mvc.problemdetails.enabled=true`; registra um segundo `@ControllerAdvice` que disputa as mesmas exceções com o handler do projeto e pode perder o `invalid_params`.

## 3. System Design Review (Análise de Impacto)
- **Segurança:** o corpo das respostas nunca revela classes, mensagens internas nem stack traces; o detalhe fica no log do servidor.
- **Arquitetura e Integração:** um único ponto de tratamento de erros; clientes tratam um só formato.
- **Tomcat (atualização):** requisições recusadas pelo Tomcat antes do Spring (ex.: URL malformada como `/cursos/%zz`) agora também saem em `ProblemDetail`, por um `ErrorReportValve` do host (`ErroTomcatConfig`) e por um `/error` próprio (`ErroContainerController`, oculto do OpenAPI). Cobertos por `ErroTomcatTest` (socket real) e `ProblemasConhecidosTest`.
- **Riscos:** o corpo do valve do Tomcat é montado à mão (JSON simples, sem `invalid_params`).

## 4. Consequências
- **Positivos:** erros uniformes em RFC 7807 (conta em "Boas práticas REST"); sem vazamento de informação.
- **Negativos:** o `type` dos erros do Spring é derivado do status, não de uma categoria própria.
