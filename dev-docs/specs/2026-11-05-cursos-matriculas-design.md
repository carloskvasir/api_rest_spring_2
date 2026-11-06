# Design: API de Cursos, Alunos e Matrículas (Trabalho Prático 2)

**Data:** 2026-11-05
**Status:** Aguardando aprovação do usuário

## 1. Intenção
Entregar o Trabalho Prático 2 (API REST Spring Boot + H2 + JPA para gerenciar Cursos, Alunos e Matrículas), reaproveitando integralmente o padrão de desenvolvimento e documentação do projeto atual (API de Livros). O trabalho é avaliado por: CRUD (2,0), JPA (2,0), Relacionamentos (2,0), Bean Validation (1,5), Paginação (1,0), Organização (1,0), Boas práticas REST (0,5). O projeto precisa subir sem erros e o professor pode pedir demonstração.

## 2. Forma de trabalho (decidido)
- O trabalho é feito **neste repositório**, substituindo totalmente o domínio de Livros/Categorias (remoção autorizada explicitamente pelo usuário).
- Quando o histórico git atual não for mais necessário, o usuário pedirá, e só então a pasta `.git` será apagada e um repositório novo iniciado. **Nada de `.git` é apagado sem pedido explícito.**

## 3. Objetivos
- Todos os requisitos do enunciado (entidades, campos, validações, endpoints, paginação, regras de negócio, README).
- Manter o padrão existente: PT-BR, camadas, DTOs (records), Lombok, `ProblemDetail` (RFC 7807), `Location` no POST, Swagger na raiz, `docs/` estático, SDRs, CHANGELOG, GEMINI.md, Docker com hot-reload, `bin/scaffold.sh`, testes (JUnit 5, Mockito, MockMvc, AssertJ).

## 4. Não objetivos / anti-objetivos
- Sem autenticação, sem banco externo (somente H2), sem front-end.
- Anti-objetivo: o projeto compilar mas o enunciado ser descumprido (ex.: expor entidade JPA direto, listar sem `Pageable`, esquecer uma das 4 regras de negócio).
- Não manter `PATCH` parcial via `Map<String,Object>` (existente em Livros) a menos que seja barato; não é exigido.

## 5. Modelo de dados
| Entidade | Campos e regras |
|---|---|
| `Curso` | `id`; `nome` (@NotBlank, @Size min 5); `cargaHoraria` (@NotNull, @Positive); `categoria` (@NotBlank); `ativo` (@NotNull). `@OneToMany(mappedBy="curso") matriculas`. `@SoftDelete`. |
| `Aluno` | `id`; `nome` (@NotBlank); `email` (@NotBlank, @Email); `idade` (@NotNull, @Min(16)); `telefone` (opcional). `@OneToMany(mappedBy="aluno") matriculas`. `@SoftDelete`. |
| `Matricula` | `id`; `dataMatricula` (LocalDate, obrigatória); `status` (obrigatório); `@ManyToOne(optional=false) aluno`; `@ManyToOne(optional=false) curso`. Sem soft delete. |

- Validações ficam no **DTO de entrada** (padrão do projeto), com mensagens em PT-BR. As entidades recebem as restrições de banco equivalentes (`nullable=false`, etc.), e o enunciado ("Bean Validation nas entidades") é atendido também com as anotações de Bean Validation nas entidades (decisão minha: duplicar nas duas pontas, pois a avaliação pede as anotações nas entidades).
- `status` da matrícula: enum `StatusMatricula` {`ATIVA`, `CONCLUIDA`, `TRANCADA`, `CANCELADA`} persistido como string; exposto como String no JSON (decisão minha).
- `dataMatricula`: no cadastro, se omitida, assume a data de hoje? **Não**: é obrigatória conforme enunciado (400 se ausente). `status` no cadastro também é obrigatório conforme enunciado.
- Soft delete via `@SoftDelete` do Hibernate (6.5, já incluso no Spring Boot 3.3.4), coluna própria `deleted`, **independente** de `Curso.ativo` (regra de negócio).

## 6. API
Rotas conforme enunciado: `POST/GET/GET{id}/PUT/DELETE` em `/cursos`, `/alunos`, `/matriculas`.
- Listagens recebem `Pageable` (`page`, `size`, `sort`), ex.: `GET /cursos?page=0&size=5&sort=nome,asc`, e retornam `Page` (conteúdo + metadados de paginação) mapeado para DTOs.
- `POST` → 201 + `Location`. `PUT` → 200. `DELETE` de curso/aluno → 204 (soft delete). Não encontrado → 404.
- `PUT /matriculas/{id}` atualiza **apenas o status** (requisito "Atualizar status da matrícula"). `DELETE /matriculas/{id}` = cancelar: muda o status para `CANCELADA`, mantém o registro, responde 204 (decisão minha). Cancelar uma matrícula já cancelada: 204 idempotente (decisão minha).
- Erros em `ProblemDetail` pelo `GlobalExceptionHandler`: 400 (validação, com `invalid_params`), 404, 409 (conflito de regra de negócio), 422 para regra de negócio de elegibilidade (decisão minha: curso inativo e aluno menor de 16 → 422; exclusão com matrículas → 409). Corpo JSON malformado ou enum inválido → 400.

## 7. Regras de negócio
1. Matrícula em curso inativo → bloqueada (422).
2. Matrícula de aluno com idade < 16 → bloqueada (422), mesmo que o aluno tenha sido cadastrado antes (a checagem no cadastro do aluno via `@Min(16)` também existe).
3. Excluir curso com matrículas → bloqueado (409).
4. Excluir aluno com matrículas → bloqueado (409).
- "Possui matrículas" considera **qualquer** matrícula, inclusive `CANCELADA` (decisão minha, interpretação literal do enunciado; o histórico preserva a integridade).
- Aluno ou curso soft-deletado não pode ser referenciado em nova matrícula (404 ao tentar).

## 8. Organização e padrões
- Pacote raiz renomeado de `com.api.livros` para `com.api.matriculas` (decisão minha); artifactId `matriculas`.
- Camadas: `controllers`, `services`, `repositories` (Spring Data JPA, nova), `models`, `dtos`, `exceptions`, `config`.
- Dependências novas: `spring-boot-starter-data-jpa`, `h2` (runtime). Console H2 habilitado em dev (`/h2-console`), banco em memória.
- `DataSeeder` reescrito com cursos, alunos e matrículas de exemplo (inclui ao menos um curso inativo para demonstração).
- `bin/scaffold.sh` atualizado para o novo padrão (JPA + Repository + paginação com `Pageable`).
- SDRs novos (índice em `dev-docs/README.md` atualizado): JPA + H2 + camada repository; `@SoftDelete`; paginação com `Pageable`; regras de negócio e códigos HTTP; decisão de matrícula cancelada em vez de removida.

## 9. Testes (obrigatórios, padrão GEMINI.md)
Unitários de service (Mockito) para as 4 regras de negócio e fluxos de CRUD; integração (MockMvc + H2) para os endpoints, paginação/ordenação, validações (mensagens) e códigos de erro. `./mvnw clean test` deve dar `BUILD SUCCESS` antes de cada commit.

## 10. Documentação a atualizar
- `README.md`: nome do aluno, descrição da API, endpoints, como executar (Docker e `./mvnw spring-boot:run`), console H2.
- `docs/openapi.json` / `docs/index.html` regenerados; `CHANGELOG.md` com intenção do pacote; `GEMINI.md` ajustado onde citar Livros; versão no `pom.xml`.
- Nome do aluno no README: **mantém Carlos Kvasir Lima** (consta hoje junto com David Marlon; o enunciado diz trabalho individual, por isso fica só o dono do repositório — decisão minha, o usuário confirma).

## 11. Entrega em commits atômicos
Dependências/config → models + repositories → DTOs + services (regras) → controllers + handler → seed/scaffold → testes → documentação (SDRs, README, CHANGELOG, OpenAPI).

## 12. Deixado ao construtor
Nomes exatos de métodos/classes dentro dos padrões acima, mensagens de validação exatas, mapeamento Page→DTO (usar `Page.map`), detalhes de configuração do H2/JPA (`ddl-auto`), versão inicial do projeto.

## 13. Calls resolvidas na revisão (decisões minhas, para conferência)
- **Enunciado:** o texto original está em `dev-docs/specs/enunciado-tp2.md`; este design é a fonte para o plano, e o enunciado prevalece em caso de conflito. Não há endpoints extras além dos listados.
- **Soft delete (escolha do usuário):** `GET/PUT/DELETE` em curso ou aluno apagado → 404. Se `@SoftDelete` der problema técnico (ex.: com `@OneToMany(mappedBy)`), cair para `@SQLDelete` + `@SQLRestriction`; só em último caso, hard delete, avisando o usuário. Sem constraints de unicidade em nome ou e-mail.
- **Matrícula duplicada (ajustado pelo usuário):** não existe conceito de período no enunciado, então uma matrícula `ATIVA` do mesmo aluno no mesmo curso é bloqueada com 409. Matrículas `CANCELADA`, `CONCLUIDA` ou `TRANCADA` não bloqueiam uma nova matrícula (permite refazer o curso). Regra extra, além das 4 do enunciado; entra no SDR de regras de negócio e nos testes.
- **Códigos HTTP:** o enunciado não os define; vale 422 (elegibilidade) e 409 (exclusão com matrículas), conforme seção 6.
- **Regra 2 (menor de 16):** como `@Min(16)` já barra o cadastro, a regra é verificada no service e testada com unitário (Mockito); o seed não inclui menor de 16.
- **Limpeza:** apagar classes, testes, seed e docs de Livros/Categorias. SDRs antigos e histórico do CHANGELOG permanecem (SDRs de stack, REST etc. continuam válidos; os que citam armazenamento em memória ganham nota de "Superado" quando for o caso).
- **PATCH:** removido nas três entidades; `bin/scaffold.sh` e OpenAPI deixam de tê-lo.
