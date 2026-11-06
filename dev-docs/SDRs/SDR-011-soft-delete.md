# SDR-011: Remoção lógica (`@SoftDelete`) em Curso e Aluno

**Data:** 2026-11-05
**Status:** Aceito

## 1. Contexto e Motivação
O enunciado exige `DELETE` de cursos e alunos, mas também proíbe excluir quem possui matrículas. Remover fisicamente registros relacionados à instituição perde histórico; queremos uma forma de "remover" com o mínimo de código, sem espalhar filtros `WHERE deleted = false` pelas consultas.

## 2. Decisão
Usar `@SoftDelete(columnName = "deleted")` do Hibernate (6.4+; o Spring Boot 3.3.4 traz o 6.5) em `Curso` e `Aluno`:
- `repository.delete(...)` executa `UPDATE ... SET deleted = true`.
- `findAll`, `findById`, `exists*` e paginação ignoram automaticamente os registros apagados; `GET/PUT/DELETE` em um recurso apagado retornam 404.
- A coluna `deleted` é **independente** do campo de negócio `Curso.ativo` (que alimenta a regra "não matricular em curso inativo").
- `Matricula` **não** usa soft delete: `DELETE /matriculas/{id}` cancela (status `CANCELADA`), preservando o histórico (SDR-013).

**Limitação descoberta na implementação:** o Hibernate 6.5 lança `UnsupportedMappingException` para associação to-one `LAZY` que aponta para entidade com `@SoftDelete`. Por isso `Matricula.aluno` e `Matricula.curso` usam o fetch padrão (`EAGER`). O custo é um join extra por matrícula, irrelevante neste escopo.
Plano B previsto (não necessário): `@SQLDelete` + `@SQLRestriction`.

## 3. System Design Review (Análise de Impacto)
- **Arquitetura e Integração:** comportamento transparente para Services e Controllers.
- **Segurança:** dados "apagados" continuam no banco (LGPD exigiria política de expurgo em um cenário real).
- **Desempenho:** toda consulta ganha o predicado `deleted = false`; sem impacto perceptível.
- **Riscos:** consultas nativas ignoram o filtro; unicidade futura (ex.: e-mail único) precisaria considerar registros apagados.

## 4. Consequências
- **Positivos:** uma anotação por entidade; histórico preservado; menos código.
- **Negativos:** `@ManyToOne` para essas entidades precisa ser `EAGER`; o console H2 mostra as linhas com `deleted = true`.
