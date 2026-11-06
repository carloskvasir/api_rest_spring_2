# Changelog

Todos os marcos e mudanças notáveis neste projeto serão documentados neste arquivo.
O formato baseia-se no [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/) e é gerado dinamicamente.

## [2.0.0](https://github.com/carloskvasir/api_rest_spring_2/releases/tag/v2.0.0) - 2026-11-05
> **Intenção da Release:** Trabalho Prático 2. O domínio de Livros deu lugar a **Cursos, Alunos e Matrículas**, com persistência real (JPA + H2), paginação via `Pageable`, remoção lógica e regras de negócio, mantendo o padrão de camadas, testes, SDRs e documentação do projeto.

### ✨ Added
- E-mail de confirmação de matrícula via Resend, opcional (`RESEND_API_KEY`), enviado após o commit e sem afetar a resposta da API (SDR-019)
- Deploy na DigitalOcean com Terraform (`infra/digitalocean`): droplet com Docker, firewall e alertas por e-mail (SDR-018)
- Entidades `Curso`, `Aluno` e `Matricula` com relacionamentos `@OneToMany`/`@ManyToOne` e camada `repositories` (SDR-010)
- CRUD completo de cursos e alunos; matrículas com atualização de status e cancelamento
- Paginação com `Pageable` (`page`, `size`, `sort`) e resposta de página estável (SDR-012)
- Regras de negócio: curso inativo (422), aluno menor de 16 anos (422), exclusão com matrículas (409) e matrícula ativa duplicada (409) (SDR-013)
- Remoção lógica com `@SoftDelete` em cursos e alunos (SDR-011)
- Seed de cursos, alunos e matrículas; console H2 em `/h2-console`
- SDR-010 a SDR-014, design, plano e enunciado em `dev-docs/`
- Erros do Spring MVC (rota inexistente, 405, 415, 406) em `ProblemDetail`, erro 500 genérico e stack trace desligado no corpo das respostas (SDR-017)
- Ordenação (`sort`) restrita a propriedades permitidas por recurso, com desempate por `id`, evitando itens repetidos entre páginas (SDR-012)
- Controle de concorrência com lock pessimista: matrícula `ATIVA` única por aluno e curso e exclusões seguras sob requisições simultâneas (SDR-016)
- Integração contínua com GitHub Actions (Java 21, `./mvnw clean test`) em push e Pull Request (SDR-015)

### 🔄 Changed
- Listagem de matrículas sem N+1 (`@EntityGraph`), telefone validado, perfil `prod` (sem console H2) e CI com runner fixo e smoke test da imagem Docker (SDR-014, SDR-015)
- Pendências conhecidas corrigidas: URL malformada (Tomcat) em `ProblemDetail`; decimais em campos inteiros e e-mail sem ponto no domínio retornam 400; `docs/openapi.json` com códigos de sucesso/erro e parâmetros de paginação; `bin/scaffold.sh` não sobrescreve arquivos; URLs de release apontam para o repositório atual (SDR-014, SDR-017)
- Pacote `com.api.livros` renomeado para `com.api.matriculas`; armazenamento em memória substituído por H2 com JPA
- `bin/scaffold.sh` gera Model JPA, Repository, DTOs, Service e Controller paginado
- README, OpenAPI (`docs/`) e `GEMINI.md` atualizados para o novo domínio
- Workflow do Gitea corrigido (Java 17 → 21 e `mvn` → `./mvnw`)

### 🗑️ Removed
- Domínio de Livros e Categorias, incluindo o endpoint `PATCH`

## [1.7.1](https://github.com/carloskvasir/api_rest_spring/releases/tag/v1.7.1) - 2026-10-02
### ⚙️ Tarefas de Manutenção (Chore)
- automatiza a atualizacao da versao do swagger durante o processo de release

## [1.7.0](https://github.com/carloskvasir/api_rest_spring/releases/tag/v1.7.0) - 2026-10-02
> *(Consulte a tag acima para ver as notas completas)*

## [1.6.2](https://github.com/carloskvasir/api_rest_spring/releases/tag/v1.6.2) - 2026-10-02
> *(Consulte a tag acima para ver as notas completas)*

## [1.6.1](https://github.com/carloskvasir/api_rest_spring/releases/tag/v1.6.1) - 2026-10-02
> *(Consulte a tag acima para ver as notas completas)*

## [1.6.0](https://github.com/carloskvasir/api_rest_spring/releases/tag/v1.6.0) - 2026-10-02
> *(Consulte a tag acima para ver as notas completas)*

## [1.5.0](https://github.com/carloskvasir/api_rest_spring/releases/tag/v1.5.0) - 2026-10-02
> *(Consulte a tag acima para ver as notas completas)*

## [1.4.0](https://github.com/carloskvasir/api_rest_spring/releases/tag/v1.4.0) - 2026-10-02
> *(Consulte a tag acima para ver as notas completas)*

## [1.3.1](https://github.com/carloskvasir/api_rest_spring/releases/tag/v1.3.1) - 2026-10-02
> *(Consulte a tag acima para ver as notas completas)*

## [1.3.0](https://github.com/carloskvasir/api_rest_spring/releases/tag/v1.3.0) - 2026-10-02
> *(Consulte a tag acima para ver as notas completas)*

## [1.2.0](https://github.com/carloskvasir/api_rest_spring/releases/tag/v1.2.0) - 2026-10-02
> *(Consulte a tag acima para ver as notas completas)*

## [1.1.0](https://github.com/carloskvasir/api_rest_spring/releases/tag/v1.1.0) - 2026-10-02
> *(Consulte a tag acima para ver as notas completas)*

## [1.0.0](https://github.com/carloskvasir/api_rest_spring/releases/tag/v1.0.0) - 2026-10-02
> *(Consulte a tag acima para ver as notas completas)*

