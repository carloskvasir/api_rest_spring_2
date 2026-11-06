# SDR-010: Persistência com Spring Data JPA, H2 e camada Repository

**Data:** 2026-11-05
**Status:** Aceito

## 1. Contexto e Motivação
O Trabalho Prático 2 (Cursos, Alunos e Matrículas) exige persistência em banco **H2** com **JPA**, relacionamentos `@OneToMany`/`@ManyToOne` e separação em camadas Controller, Repository e Entity. O projeto anterior armazenava os dados em listas em memória dentro dos Services (SDR-001), o que não atende ao enunciado nem permite consultas relacionais (ex.: "este curso possui matrículas?").

## 2. Decisão
- Adicionar `spring-boot-starter-data-jpa` e `com.h2database:h2` (escopo `runtime`).
- Criar a camada `repositories` com interfaces `JpaRepository` (`CursoRepository`, `AlunoRepository`, `MatriculaRepository`); consultas derivadas (`existsByCursoId`, `existsByAlunoId`, `existsByAlunoIdAndCursoIdAndStatus`) sustentam as regras de negócio.
- Banco H2 **em memória** (`jdbc:h2:mem:matriculas`), `ddl-auto=create-drop`, console em `/h2-console` para demonstração, `open-in-view=false` (sem lazy loading na camada web).
- As entidades JPA **não** são expostas pela API: continuam os DTOs (records) do padrão do projeto. Entidades usam `@Getter/@Setter` do Lombok, sem `@Data`, para evitar `equals/hashCode/toString` recursivos nos relacionamentos bidirecionais.
- Services ganham `@Transactional` (escrita) e `@Transactional(readOnly = true)` (leitura).
- Alternativas descartadas: manter listas em memória (não atende ao enunciado); Flyway/Liquibase (excesso para o escopo; `create-drop` basta com H2 em memória).

## 3. System Design Review (Análise de Impacto)
- **Arquitetura e Integração:** `controllers → services → repositories → models`. O `DataSeeder` passa pelos services, reaproveitando as regras de negócio.
- **Segurança:** o console H2 está habilitado e **não** deve ir para produção; o banco é descartável e sem dados reais.
- **Docker:** com `network_mode: host`, o console H2 falhava (500, `UnknownHostException`) por tentar resolver o hostname da máquina dentro do contêiner. O `docker-compose.yml` define `JAVA_TOOL_OPTIONS=-Dh2.bindAddress=127.0.0.1` para evitar essa consulta (descoberto no teste de ponta a ponta em contêiner).
- **Desempenho e Escalabilidade:** consultas `exists*` evitam carregar coleções; paginação no banco (SDR-012).
- **Riscos:** dados somem a cada reinício (aceitável no escopo); trocar para outro banco exige apenas dependência e `datasource`.

## 4. Consequências
- **Positivos:** atende ao enunciado; integridade relacional; regras de negócio baseadas em consultas.
- **Negativos:** testes de integração sobem contexto JPA (alguns segundos a mais); SDR-001 e SDR-003 deixam de valer na parte "em memória".
