# SDR-013: Regras de negócio, cancelamento de matrícula e códigos HTTP

**Data:** 2026-11-05
**Status:** Aceito

## 1. Contexto e Motivação
O enunciado lista 4 regras que a aplicação deve impedir (curso inativo, aluno menor de 16, exclusão de curso e de aluno com matrículas) mas não define códigos HTTP nem o que significa "cancelar" matrícula.

## 2. Decisão
| Situação | Resposta |
|---|---|
| Matrícula em curso inativo | **422** (`BusinessRuleException`) |
| Matrícula de aluno com idade < 16 | **422** (`BusinessRuleException`) |
| Excluir curso ou aluno com matrículas (de **qualquer** status) | **409** (`ConflictException`) |
| Nova matrícula `ATIVA` do mesmo aluno no mesmo curso já tendo uma `ATIVA` (regra extra) | **409** |
| Página solicitada com offset acima de `Integer.MAX_VALUE` (ex.: `page=2147483647&size=100`) | **400** |
| Aluno/curso/matrícula inexistente ou removido logicamente | **404** |
| Validação de campos, JSON malformado, enum inválido, `sort` inválido | **400** |

- Ordem de validação em `realizar`: aluno (404) → curso (404) → curso ativo (422) → idade (422) → duplicata ativa (409).
- A regra de duplicidade **não** bloqueia quando a matrícula anterior está `CANCELADA`, `CONCLUIDA` ou `TRANCADA` (permite refazer o curso); vale também ao reativar via `PUT` para `ATIVA`. Nesse caso a reativação também reaplica a elegibilidade (curso ativo e aluno com 16+ anos, 422), para que o `PUT` não contorne a regra de matrícula em curso inativo. Concluir ou cancelar em curso inativo continua permitido. Não existe conceito de período no enunciado.
- `status` é o enum `StatusMatricula {ATIVA, CONCLUIDA, TRANCADA, CANCELADA}`, persistido como texto e exposto como string.
- `PUT /matriculas/{id}` altera **apenas** o status. `DELETE /matriculas/{id}` **cancela** (status `CANCELADA`, 204, idempotente) e não apaga a linha.
- O `PATCH` do projeto anterior foi removido (não exigido; ver SDR-002).
- A regra de idade mínima existe em dois lugares: `@Min(16)` no cadastro do aluno e checagem no Service de matrícula (testada com unitário, pois o cadastro já barra menores).

## 3. System Design Review (Análise de Impacto)
- **Arquitetura:** exceções de negócio tratadas centralmente no `GlobalExceptionHandler`, sempre em `ProblemDetail` (RFC 7807, SDR-002).
- **Riscos:** 422 vs 400 para elegibilidade é uma escolha (o enunciado não define); documentada aqui e coberta por testes.

## 4. Consequências
- **Positivos:** semântica HTTP clara; histórico de matrículas preservado; regras isoladas e testáveis.
- **Negativos:** curso/aluno com qualquer matrícula (até cancelada) nunca pode ser excluído.
