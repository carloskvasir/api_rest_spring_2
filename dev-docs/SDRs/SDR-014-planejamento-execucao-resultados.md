# SDR-014: Planejamento, Execução e Resultados do Trabalho Prático 2

**Data:** 2026-11-05
**Status:** Aceito

## 1. Contexto e Motivação
O Trabalho Prático 2 (API de Cursos, Alunos e Matrículas) reaproveita o projeto do Trabalho 1 (Livros). Este SDR registra como o trabalho foi planejado, executado e o que foi entregue, para rastreabilidade e para apoiar a demonstração ao professor.

## 2. Planejamento
- **Enunciado:** [`dev-docs/specs/enunciado-tp2.md`](../specs/enunciado-tp2.md).
- **Design aprovado:** [`dev-docs/specs/2026-11-05-cursos-matriculas-design.md`](../specs/2026-11-05-cursos-matriculas-design.md), com escopo, regras, decisões e anti-objetivos. Um revisor independente leu o design antes da aprovação; as lacunas apontadas (matrícula duplicada, códigos HTTP, soft delete, limpeza do domínio antigo, remoção do `PATCH`) foram resolvidas e registradas na seção 13 do design.
- **Plano de implementação:** [`dev-docs/plans/2026-11-05-cursos-matriculas-plan.md`](../plans/2026-11-05-cursos-matriculas-plan.md), 9 tarefas em TDD, commits atômicos e `./mvnw clean test` verde antes de cada commit.
- **Decisões de escopo do usuário:** trabalhar neste repositório substituindo totalmente o domínio de Livros; usar remoção lógica (SDR-011); ajustar a regra de matrícula duplicada (SDR-013); recriar o histórico git só quando o usuário pedir.

## 3. Execução
> **Nota sobre o histórico git:** a execução abaixo ocorreu em 12 commits atômicos na branch `feat/cursos-matriculas` do repositório do Trabalho 1. A pedido do usuário, o repositório foi recriado com dois commits (base v1.7.1 e entrega do Trabalho 2); os hashes da tabela referem-se ao histórico original, preservado em backup local fora do projeto.

Cada tarefa seguiu RED → GREEN (o teste falhou antes de existir código de produção).

| Tarefa | Commit | Conteúdo | Testes ao final |
|---|---|---|---|
| 1 | `3ba32e2` | Remove Livros, renomeia pacote para `com.api.matriculas`, adiciona JPA + H2 | 1 |
| 2 | `6c25919` | Entidades, enum e repositories (`@SoftDelete`) | 7 |
| 3 | `5c1fd41` | `BusinessRuleException`, `ConflictException` e handlers (400/404/409/422) | 15 |
| 4 | `072798e` | CRUD de Cursos com paginação | 31 |
| 5 | `4a7fe0b` | CRUD de Alunos | 43 |
| 6 | `359f1ce` | Matrículas e regras de negócio | 66 |
| 7 | `a15fcf7` | Seed e gerador de scaffold atualizado | 70 |
| 8 | `7f9bd8a`, `0e9f45a`, `a567382` | SDRs, README, CHANGELOG, OpenAPI (versão 2.0.0) | 70 |
| Revisão | (commit de correções) | Correções da revisão final independente | 74 |
| Pós-revisão | (commits finais) | CI (SDR-015), console H2 no Docker e controle de concorrência (SDR-016) e ordenação restrita a propriedades permitidas (SDR-012) e erros padronizados (SDR-017) | 102 |

**Desvios e descobertas durante a execução** (todos cobertos por teste ou validação):
1. `@ManyToOne(fetch = LAZY)` para entidade com `@SoftDelete` é recusado pelo Hibernate 6.5 → fetch padrão (SDR-011).
2. `@EnableSpringDataWebSupport` desliga as propriedades `spring.data.web.pageable.*` → limites configurados por customizer (SDR-012). O teste `size=100000` detectou o problema.
3. `PUT` de status para `ATIVA` também valida duplicidade (SDR-013).
4. `DataSeederTest` usa um H2 próprio porque os demais testes limpam as tabelas.
5. **Revisão final independente da branch** (leitura do código, 70 testes e cerca de 60 cenários manuais) encontrou dois pontos corrigidos por TDD (teste vermelho, depois verde): (a) `PUT /matriculas/{id}` com `ATIVA` reativava matrícula em curso inativo, contornando a regra 1 do enunciado; (b) `page=2147483647&size=100` retornava 500 com stack trace, agora 400 em `ProblemDetail`.

## 4. Resultados
- `./mvnw clean test`: **102 testes, 0 falhas, `BUILD SUCCESS`** (unitários com Mockito, `@DataJpaTest`, `@WebMvcTest` e integração com MockMvc + H2).
- Validação manual com `curl` na aplicação em execução (`./mvnw spring-boot:run`): listagem paginada e ordenada (200); matrícula duplicada ativa (409); curso inativo (422); matrícula válida (201); excluir curso e aluno com matrículas (409); validação de aluno com e-mail e idade inválidos (400 com `invalid_params`); cancelamento (204) mantendo o registro com status `CANCELADA`; `sort` inválido (400); `/h2-console` e Swagger UI na raiz (200).
- Após as correções da revisão, a aplicação em execução confirmou: reativar matrícula em curso inativo → 422; página com offset gigante → 400.
- A aplicação inicia sem erros e semeia 4 cursos (1 inativo), 4 alunos e 4 matrículas.

| Critério do enunciado | Onde é atendido |
|---|---|
| CRUD (2,0) | `CursoController`, `AlunoController`, `MatriculaController` + testes de controller |
| JPA (2,0) | Entidades e repositories, SDR-010, `RepositoriosTest` |
| Relacionamentos (2,0) | `@OneToMany(mappedBy)` em Curso/Aluno, `@ManyToOne(optional = false)` em Matrícula |
| Bean Validation (1,5) | Anotações em DTOs e entidades; erros em `ProblemDetail` com `invalid_params` |
| Paginação (1,0) | `Pageable` em todas as listagens, SDR-012 |
| Organização (1,0) | Camadas controllers/services/repositories/models/dtos/exceptions |
| Boas práticas REST (0,5) | Verbos e status corretos, `Location` no POST, RFC 7807, SDR-013 |

## 5. Pendências conhecidas (adiadas de propósito)
- ~~Condição de corrida em matrículas `ATIVA` duplicadas~~ — **corrigida** com lock pessimista (SDR-016).
- ~~`sort` por coleção repetia itens entre páginas~~ — **corrigido** com lista de propriedades ordenáveis e desempate por `id` (SDR-012).
- ~~Erros 404 de rota, 405 e 415 fora de `ProblemDetail` e stack trace exposto~~ — **corrigido** (SDR-017). A página HTML do Tomcat para URLs malformadas também foi substituída por `ProblemDetail`.
- ~~`docs/openapi.json` só com código 200 e `Pageable` como objeto~~ — **corrigido**: `OperationCustomizer` documenta 201/204/400/404/409/422/500 com o schema `ProblemDetail` e `@ParameterObject` expõe `page`, `size` e `sort`; o JSON foi regenerado.
- ~~Jackson truncava decimais (`"cargaHoraria": 1.7` virava 1)~~ — **corrigido** com `spring.jackson.deserialization.accept-float-as-int=false` (400).
- ~~`@Email` aceitava `a@b`~~ — **corrigido** com regex que exige ponto no domínio (`Aluno.EMAIL_REGEX`).
- ~~`bin/scaffold.sh` sobrescrevia arquivos existentes~~ — **corrigido**: aborta sem alterar nada se algum alvo já existir.
- ~~URLs do repositório antigo em `release.sh`, `generate_changelog.js`, `cliff.toml` e CHANGELOG 2.0.0~~ — **corrigidas** (links das versões 1.x permanecem, pois as tags estão no repositório antigo).

## 6. Consequências
- **Positivos:** entrega rastreável, com decisões, desvios e evidências documentados.
- **Negativos:** o histórico do Trabalho 1 permanece no CHANGELOG e nos SDRs 001 a 009 até a recriação do repositório git.
