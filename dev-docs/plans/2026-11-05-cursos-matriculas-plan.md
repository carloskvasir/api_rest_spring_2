# API de Cursos, Alunos e Matrículas — Plano de Implementação

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Substituir o domínio Livros/Categorias por Cursos/Alunos/Matrículas com JPA + H2, paginação via `Pageable`, soft delete e regras de negócio, mantendo o padrão de código e documentação do projeto.

**Architecture:** Camadas `controllers → services → repositories → models`, DTOs em records com `fromEntity`, erros centralizados em `ProblemDetail`. Persistência Spring Data JPA em H2 em memória; `@SoftDelete` do Hibernate em `Curso` e `Aluno`.

**Tech Stack:** Java 21, Spring Boot 3.3.4 (Hibernate 6.5), Maven wrapper, Lombok, spring-boot-starter-data-jpa, H2, springdoc 2.6.0, JUnit 5 / Mockito / AssertJ / MockMvc.

**Spec:** `dev-docs/specs/2026-11-05-cursos-matriculas-design.md` (enunciado original em `dev-docs/specs/enunciado-tp2.md`; o enunciado prevalece em conflito).

## Global Constraints

- Idioma de código (exceto termos do framework), documentação e commits: PT-BR; commits no padrão Conventional Commits.
- Pacote raiz `com.api.matriculas`; artifactId `matriculas`; Java 21; Spring Boot 3.3.4.
- Nunca expor entidades nos endpoints: sempre DTOs (records).
- Erros sempre em `ProblemDetail` (RFC 7807) via `@ControllerAdvice`; POST devolve 201 + `Location`.
- Antes de **cada** commit: `./mvnw clean test` com `BUILD SUCCESS`. Commits atômicos.
- Toda funcionalidade tem teste automatizado; todo novo pacote/decisão gera SDR em `dev-docs/SDRs/` + atualização do índice `dev-docs/README.md`.
- Regras: curso inativo → 422; aluno < 16 → 422; excluir curso/aluno com matrículas (qualquer status) → 409; matrícula `ATIVA` duplicada (mesmo aluno+curso) → 409; recurso ou referência inexistente/apagado → 404; validação → 400 com `invalid_params`.
- `status` da matrícula: enum `StatusMatricula {ATIVA, CONCLUIDA, TRANCADA, CANCELADA}`, persistido como string. `DELETE /matriculas/{id}` = status `CANCELADA`, 204, idempotente. `PUT /matriculas/{id}` altera só o status.
- Listagens: `Pageable` (`page`, `size`, `sort`) retornando `Page<…ResponseDTO>`.
- Sem `PATCH`. Sem autenticação. H2 apenas.
- Não apagar `.git` (só quando o usuário pedir).
- Autorizado pelo usuário: apagar totalmente o domínio Livros/Categorias.

## Review Focus

1. `GET /cursos?sort=campoInexistente` → resposta 4xx em `ProblemDetail`, não 500 (Task 4).
2. JSON malformado ou `status` fora do enum → 400 em `ProblemDetail` (Task 3 e 6).
3. `size=0` / `page` negativo / `size` enorme na paginação não derrubam a API (Task 4).
4. Matrícula referenciando aluno/curso inexistente ou soft-deletado → 404; `cargaHoraria`/`idade` nulos → 400, não 500 (Tasks 5, 6).
5. Cancelar duas vezes a mesma matrícula e refazer matrícula após cancelada (Task 6).

## Estrutura de arquivos

Em `src/main/java/com/api/matriculas/`: `MatriculasApplication`; `config/{OpenApiConfig,DataSeeder}`; `models/{Curso,Aluno,Matricula,StatusMatricula}`; `repositories/{CursoRepository,AlunoRepository,MatriculaRepository}`; `dtos/{Curso,Aluno,Matricula}{Request,Response}DTO` + `MatriculaStatusRequestDTO`; `services/{Curso,Aluno,Matricula}Service`; `controllers/{Curso,Aluno,Matricula}Controller`; `exceptions/{GlobalExceptionHandler,ResourceNotFoundException,BusinessRuleException,ConflictException}`. Testes espelham a estrutura em `src/test/java/com/api/matriculas/`.

---

### Task 1: Esqueleto — remover Livros, renomear pacote, adicionar JPA + H2

**Files:**
- Delete: todo `src/main/java/com/api/livros/**` e `src/test/java/com/api/livros/**` (autorizado)
- Create: `MatriculasApplication.java`, `config/OpenApiConfig.java` (portar com título "API de Cursos e Matrículas"), `exceptions/{GlobalExceptionHandler,ResourceNotFoundException}.java` (portados do existente, URIs `https://api.matriculas.com/errors/...`), `src/test/.../MatriculasApplicationTests.java`
- Modify: `pom.xml` (artifactId `matriculas`, name/description, dependências `spring-boot-starter-data-jpa` e `com.h2database:h2` runtime), `src/main/resources/application.properties`, `docker-compose.yml`, `bin/release.sh` (referências a livros), `Dockerfile` se citar jar

**Interfaces:** Produces: `ResourceNotFoundException(String)`; `GlobalExceptionHandler` com handlers de validação (400) e not-found (404).

- [ ] **Step 1:** Criar branch `feat/cursos-matriculas` a partir de `main`.
- [ ] **Step 2:** Escrever `MatriculasApplicationTests.contextLoads()` (`@SpringBootTest`). Rodar `./mvnw clean test` — deve FALHAR (classe de aplicação inexistente).
- [ ] **Step 3:** Mover/renomear código mantido para `com.api.matriculas`, remover tudo de Livros/Categorias (incluindo `DataSeeder` por ora — será recriado na Task 7), ajustar `pom.xml`, `docker-compose.yml`, `bin/release.sh`. `application.properties`: `springdoc.swagger-ui.path=/`, `spring.datasource.url=jdbc:h2:mem:matriculas;DB_CLOSE_DELAY=-1`, `spring.jpa.hibernate.ddl-auto=create-drop`, `spring.jpa.open-in-view=false`, `spring.h2.console.enabled=true`.
- [ ] **Step 4:** `./mvnw clean test` → `BUILD SUCCESS`. Conferir `grep -ri livro src pom.xml docker-compose.yml bin Dockerfile` sem resultados.
- [ ] **Step 5:** Commit `chore: remove dominio de livros e adiciona JPA com H2`.

---

### Task 2: Entidades e repositórios

**Files:**
- Create: `models/{Curso,Aluno,Matricula,StatusMatricula}.java`, `repositories/{CursoRepository,AlunoRepository,MatriculaRepository}.java`
- Test: `src/test/.../repositories/RepositoriosTest.java` (`@DataJpaTest`)

**Interfaces — Produces:**
- `Curso` (`@Entity @SoftDelete(columnName="deleted")`, Lombok `@Getter @Setter @NoArgsConstructor`, **sem** `@Data` por causa dos ciclos): `Long id; String nome; Integer cargaHoraria; String categoria; Boolean ativo; List<Matricula> matriculas` (`@OneToMany(mappedBy="curso")`, `@Setter(NONE)`). Anotações Bean Validation conforme spec §5.
- `Aluno` (`@SoftDelete`): `Long id; String nome; String email; Integer idade; String telefone; List<Matricula> matriculas`. Validações conforme spec §5.
- `Matricula` (sem soft delete): `Long id; LocalDate dataMatricula; StatusMatricula status` (`@Enumerated(STRING)`); `Aluno aluno; Curso curso` (`@ManyToOne(optional=false, fetch=LAZY)`).
- `CursoRepository extends JpaRepository<Curso,Long>`; `AlunoRepository extends JpaRepository<Aluno,Long>`;
  `MatriculaRepository extends JpaRepository<Matricula,Long>` com `boolean existsByCursoId(Long)`, `boolean existsByAlunoId(Long)`, `boolean existsByAlunoIdAndCursoIdAndStatus(Long, Long, StatusMatricula)`.

- [ ] **Step 1:** Testes `@DataJpaTest` (nomes): `deveSalvarCursoComMatricula`, `deleteDeCursoFazSoftDeleteEOcultaDoFindById`, `deleteDeCursoNaoApagaLinhaFisica` (conferir via `JdbcTemplate`: `select deleted from curso`), `existsByCursoIdDetectaMatricula`, `existsByAlunoIdAndCursoIdAndStatusDistingueStatus`, `matriculaSemAlunoFalha` (constraint). Rodar → FALHA.
- [ ] **Step 2:** Implementar entidades e repositories conforme as interfaces. Se `@SoftDelete` falhar com `mappedBy`, aplicar o fallback do spec §13 e registrar no SDR.
- [ ] **Step 3:** `./mvnw clean test` → PASS.
- [ ] **Step 4:** Commit `feat: adiciona entidades Curso, Aluno e Matricula com repositories`.

---

### Task 3: Exceções de negócio e handler

**Files:**
- Create: `exceptions/BusinessRuleException.java` (→422), `exceptions/ConflictException.java` (→409)
- Modify: `exceptions/GlobalExceptionHandler.java`
- Test: `src/test/.../exceptions/GlobalExceptionHandlerTest.java` (`@WebMvcTest` com controller stub interno ao teste)

**Interfaces — Produces:** `BusinessRuleException(String)`, `ConflictException(String)`, ambas `RuntimeException`. Handler adicional: `HttpMessageNotReadableException` → 400 (`invalid_params` ausente, detail "Corpo da requisição inválido ou malformado."); `PropertyReferenceException` e `IllegalArgumentException` de sort → 400; `MethodArgumentTypeMismatchException` → 400. Todos com `title`, `type`, `timestamp`.

- [ ] **Step 1:** Testes: cada exceção → status e `title` esperados; JSON malformado → 400; validação → 400 com `invalid_params`. FALHA.
- [ ] **Step 2:** Implementar. Step 3: testes PASS. Step 4: Commit `feat: trata regras de negocio e erros de payload com ProblemDetail`.

---

### Task 4: CRUD de Cursos

**Files:**
- Create: `dtos/CursoRequestDTO.java`, `dtos/CursoResponseDTO.java`, `services/CursoService.java`, `controllers/CursoController.java`
- Test: `services/CursoServiceTest.java` (Mockito), `controllers/CursoControllerTest.java` (`@SpringBootTest @AutoConfigureMockMvc`)

**Interfaces:**
- Consumes: `CursoRepository`, `MatriculaRepository.existsByCursoId`, `ResourceNotFoundException`, `ConflictException`.
- Produces: `record CursoRequestDTO(@NotBlank @Size(min=5) String nome, @NotNull @Positive Integer cargaHoraria, @NotBlank String categoria, @NotNull Boolean ativo)`; `record CursoResponseDTO(Long id, String nome, Integer cargaHoraria, String categoria, Boolean ativo)` com `static fromEntity(Curso)`; `CursoService`: `Curso salvar(CursoRequestDTO)`, `Page<Curso> listar(Pageable)`, `Curso buscarPorId(Long)` (404 "Curso não encontrado com id: X"), `Curso atualizar(Long, CursoRequestDTO)`, `void remover(Long)` (409 se `existsByCursoId`). Controller em `/cursos`.

- [ ] **Step 1:** Testes de service: salvar, buscar inexistente→404, remover com matrícula→`ConflictException`, remover sem matrícula→`repository.delete`. Testes MockMvc: POST 201+Location; POST nome "abc" → 400 com `invalid_params.nome`; `cargaHoraria=0` → 400; GET paginado `?page=0&size=2&sort=nome,asc` ordenado e com `totalElements`; `sort=campoInexistente` → 400 ProblemDetail (Review Focus 1); `size=0` e `page=-1` não retornam 500 (Review Focus 3; aceitar o default do Spring ou 400, mas nunca 500); GET/PUT/DELETE id inexistente → 404; DELETE ok → 204 e GET seguinte → 404. FALHA.
- [ ] **Step 2:** Implementar. Listagem com `@PageableDefault(size=10)`; limitar `size` máximo via `spring.data.web.pageable.max-page-size=100`.
- [ ] **Step 3:** `./mvnw clean test` PASS. **Step 4:** Commit `feat: implementa CRUD de cursos com paginacao`.

---

### Task 5: CRUD de Alunos

**Files:** análogo à Task 4: `dtos/AlunoRequestDTO`, `dtos/AlunoResponseDTO`, `services/AlunoService`, `controllers/AlunoController`; testes `AlunoServiceTest`, `AlunoControllerTest`.

**Interfaces:** `record AlunoRequestDTO(@NotBlank String nome, @NotBlank @Email String email, @NotNull @Min(16) Integer idade, String telefone)`; `AlunoResponseDTO(Long id, String nome, String email, Integer idade, String telefone)` com `fromEntity`; `AlunoService` com os mesmos 5 métodos do `CursoService`, `remover` bloqueado por `MatriculaRepository.existsByAlunoId` (409). Controller em `/alunos`.

- [ ] **Step 1:** Testes: e-mail inválido → 400 com mensagem em `invalid_params.email`; `idade=15` → 400; `idade` ausente → 400 (não 500); telefone ausente → 201; paginação/ordenação em `/alunos`; delete com matrícula → 409; delete sem → 204 e GET → 404. FALHA.
- [ ] **Step 2:** Implementar. **Step 3:** PASS. **Step 4:** Commit `feat: implementa CRUD de alunos com paginacao`.

---

### Task 6: Matrículas e regras de negócio

**Files:** `dtos/MatriculaRequestDTO`, `dtos/MatriculaStatusRequestDTO`, `dtos/MatriculaResponseDTO`, `services/MatriculaService`, `controllers/MatriculaController`; testes `MatriculaServiceTest` (Mockito), `MatriculaControllerTest`.

**Interfaces:**
- Consumes: `CursoService.buscarPorId`, `AlunoService.buscarPorId` (404 em apagado), `MatriculaRepository`.
- Produces: `record MatriculaRequestDTO(@NotNull LocalDate dataMatricula, @NotNull StatusMatricula status, @NotNull Long alunoId, @NotNull Long cursoId)`; `record MatriculaStatusRequestDTO(@NotNull StatusMatricula status)`; `record MatriculaResponseDTO(Long id, LocalDate dataMatricula, String status, AlunoResponseDTO aluno, CursoResponseDTO curso)` com `fromEntity`. `MatriculaService`: `Matricula realizar(MatriculaRequestDTO)`, `Page<Matricula> listar(Pageable)`, `Matricula buscarPorId(Long)`, `Matricula atualizarStatus(Long, StatusMatricula)`, `void cancelar(Long)`.
- Ordem de validação em `realizar`: aluno existe (404) → curso existe (404) → curso ativo (422) → idade ≥ 16 (422) → sem matrícula `ATIVA` duplicada (409, só checa quando o status novo é `ATIVA`).

- [ ] **Step 1:** Testes de service (Mockito): `realizarEmCursoInativoLancaBusinessRule`, `realizarAlunoMenorDe16LancaBusinessRule` (aluno com idade 15 montado direto no mock), `realizarDuplicataAtivaLancaConflict`, `realizarAposCanceladaPermite` (Review Focus 5), `cancelarDuasVezesNaoFalha` (Review Focus 5), `atualizarStatusAlteraApenasStatus`. Testes MockMvc: POST 201+Location com aluno e curso válidos (criar via repositories); alunoId inexistente → 404; curso inativo → 422; status inválido `"XPTO"` → 400 (Review Focus 2); `dataMatricula` ausente → 400; PUT status → 200; DELETE → 204 e GET mostra `CANCELADA`; GET paginado `/matriculas?page=0&size=5&sort=dataMatricula,desc`; aluno/curso apagados (soft) → 404 ao matricular (Review Focus 4). FALHA.
- [ ] **Step 2:** Implementar. **Step 3:** PASS. **Step 4:** Commit `feat: implementa matriculas e regras de negocio`.

---

### Task 7: Seed e scaffold

**Files:** Create `config/DataSeeder.java`; Modify `bin/scaffold.sh`; Test `config/DataSeederTest` (contagens após boot).

**Interfaces:** `DataSeeder implements CommandLineRunner` usando os services; semeia ≥4 cursos (≥1 inativo), ≥4 alunos (todos ≥16), ≥3 matrículas. `scaffold.sh <Entidade>` passa a gerar Model JPA (`@Entity`), Repository, DTOs, Service com `Pageable` e Controller paginado, sem listas em memória nem PATCH, pacote `com.api.matriculas`.

- [ ] **Step 1:** Teste do seeder (conta registros via repositories). FALHA. **Step 2:** Implementar seeder e reescrever o scaffold; rodar `./bin/scaffold.sh Teste` num diretório temporário/branch descartável e confirmar `./mvnw compile`, depois descartar o gerado. **Step 3:** PASS. **Step 4:** Commit `feat: adiciona seed de dados e atualiza gerador de scaffold`.

---

### Task 8: Documentação e SDRs (planejamento, execução, resultados)

**Files:**
- Create em `dev-docs/SDRs/` (copiar `SDR-TEMPLATE.md`; numeração sequencial a partir da 010): JPA + H2 + camada repository; `@SoftDelete`; paginação com `Pageable`; regras de negócio e códigos HTTP (inclui matrícula duplicada e cancelamento); **SDR de Planejamento, Execução e Resultados** (links para design e plano, linha do tempo dos commits, desvios do plano, fallback do `@SoftDelete` se usado, resultado final: saída de `./mvnw clean test` com nº de testes, critérios do enunciado ↔ onde são atendidos).
- Modify: `dev-docs/README.md` (índice), SDRs antigos que ficaram obsoletos (SDR-001/003/008: nota de "Superado" ou ajuste de exemplos de Livros), `README.md` (nome do aluno Carlos Kvasir Lima, descrição, tabela de endpoints, como executar com Docker e `./mvnw spring-boot:run`, console H2 `/h2-console` com JDBC URL, link do Swagger), `GEMINI.md` (referências a Livros e camada `repositories`), `CHANGELOG.md` (nova entrada com "Intenção da Release"), `docs/openapi.json` e `docs/index.html` (regenerar com o fluxo existente em `bin/release.sh`/SDR-007; subir a aplicação, baixar `/v3/api-docs`), versão do `pom.xml` conforme o processo de release (minor, ex.: 2.0.0, por quebra de domínio).

- [ ] **Step 1:** Subir a app (`./mvnw spring-boot:run`), validar manualmente com `curl`: criar curso/aluno, matricular, duplicar (409), curso inativo (422), listar com paginação, deletar curso com matrícula (409), `/h2-console` acessível; guardar os resultados para o SDR.
- [ ] **Step 2:** Escrever SDRs, README, GEMINI.md, CHANGELOG, regenerar OpenAPI. **Step 3:** Conferir links do índice e `grep -ri livro` fora do CHANGELOG histórico e SDRs antigos. **Step 4:** Commits atômicos: `docs: adiciona SDRs de JPA, soft delete, paginacao e regras de negocio`; `docs: atualiza README, GEMINI e OpenAPI para cursos e matriculas`; `docs: registra planejamento, execucao e resultados no SDR`.

---

### Task 9: Verificação final

- [ ] **Step 1:** `./mvnw clean test` → `BUILD SUCCESS`; anotar nº de testes. **Step 2:** `./mvnw spring-boot:run` sobe sem erros; `docker compose build` opcional se Docker disponível. **Step 3:** Conferir cada linha da tabela de critérios do enunciado contra o comportamento real (executar os `curl`). **Step 4:** Completar o SDR de resultados com os números reais e commitar `docs: registra resultados finais`. **Step 5:** Reportar ao usuário; criação de tag/release e eventual remoção de `.git` só mediante pedido.

---

## Auto-revisão

- Cobertura do spec: entidades/validações (T2, T4–T6), rotas e paginação (T4–T6), 4 regras + duplicada (T4–T6), soft delete (T2), erros (T3), seed/scaffold (T7), SDRs/README/CHANGELOG/OpenAPI/GEMINI (T8), testes em todas as tasks.
- Tipos consistentes: `listar(Pageable)`, `buscarPorId(Long)`, `remover(Long)` em T4/T5; `MatriculaService` consome `CursoService`/`AlunoService` definidos antes.
- Nota: `Matricula.status` é enum internamente e String no JSON (`MatriculaResponseDTO.status`).
