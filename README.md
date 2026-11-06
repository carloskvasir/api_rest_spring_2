# API REST de Cursos, Alunos e Matrículas

> 🔗 **Repositório no GitHub:** [carloskvasir/api_rest_spring_2](https://github.com/carloskvasir/api_rest_spring_2)
> _(Caso esteja avaliando este projeto a partir de um backup .zip, utilize o link acima para acessar o código online atualizado)._

> 🧬 **Origem:** este projeto é derivado do [api_rest_spring](https://github.com/carloskvasir/api_rest_spring) (Trabalho 1, API de Livros, versão 1.7.1). O primeiro commit deste repositório é uma cópia fiel desse projeto base; o commit seguinte o transforma na API de Cursos, Alunos e Matrículas, mantendo o padrão de arquitetura, testes e documentação.

**Trabalho Prático 2 — Desenvolvimento de API REST com Spring Boot.** API RESTful em **Java 21 + Spring Boot 3.3** para gerenciar **cursos**, **alunos** e **matrículas**, com persistência em **H2** via **Spring Data JPA**, **Bean Validation**, **paginação** com `Pageable` e regras de negócio. Os erros seguem a RFC 7807 (`ProblemDetail`) e as decisões de arquitetura estão registradas em SDRs (System Design Records).

## 👥 Aluno
* Carlos Kvasir Lima

## 📝 Descrição da API

| Entidade | Campos | Regras |
|---|---|---|
| **Curso** | `id`, `nome`, `cargaHoraria`, `categoria`, `ativo` | nome obrigatório (mín. 5 caracteres); carga horária > 0; categoria e ativo obrigatórios |
| **Aluno** | `id`, `nome`, `email`, `idade`, `telefone` | nome obrigatório; e-mail válido; idade mín. 16; telefone opcional |
| **Matrícula** | `id`, `dataMatricula`, `status`, `aluno`, `curso` | data e status obrigatórios; aluno e curso obrigatórios. `status`: `ATIVA`, `CONCLUIDA`, `TRANCADA`, `CANCELADA`. No `POST` o aluno e o curso são informados por `alunoId` e `cursoId`; a resposta traz os objetos `aluno` e `curso` completos |

Relacionamentos: um curso e um aluno possuem várias matrículas (`@OneToMany`); cada matrícula pertence a um aluno e a um curso (`@ManyToOne`).

### Regras de negócio
| Situação | Resposta |
|---|---|
| Matrícula em curso inativo | `422 Unprocessable Entity` |
| Matrícula de aluno menor de 16 anos | `422 Unprocessable Entity` |
| Excluir curso ou aluno que possua matrículas | `409 Conflict` |
| Nova matrícula `ATIVA` para quem já tem uma `ATIVA` no mesmo curso (regra extra) | `409 Conflict` |

Curso e aluno são removidos **logicamente** (`@SoftDelete`). `DELETE /matriculas/{id}` **cancela** a matrícula (status `CANCELADA`) e mantém o registro.

## 🔌 Endpoints

| Recurso | Método e rota | Descrição | Sucesso |
|---|---|---|---|
| Cursos | `POST /cursos` | Cadastrar curso | 201 + `Location` |
| | `GET /cursos` | Listar (paginado) | 200 |
| | `GET /cursos/{id}` | Buscar por ID | 200 |
| | `PUT /cursos/{id}` | Atualizar curso | 200 |
| | `DELETE /cursos/{id}` | Remover curso | 204 |
| Alunos | `POST /alunos` | Cadastrar aluno | 201 + `Location` |
| | `GET /alunos` | Listar (paginado) | 200 |
| | `GET /alunos/{id}` | Buscar por ID | 200 |
| | `PUT /alunos/{id}` | Atualizar aluno | 200 |
| | `DELETE /alunos/{id}` | Remover aluno | 204 |
| Matrículas | `POST /matriculas` | Realizar matrícula | 201 + `Location` |
| | `GET /matriculas` | Listar (paginado) | 200 |
| | `GET /matriculas/{id}` | Buscar por ID | 200 |
| | `PUT /matriculas/{id}` | Atualizar **somente o status** (`{"status": "CONCLUIDA"}`) | 200 |
| | `DELETE /matriculas/{id}` | Cancelar matrícula | 204 |

**Paginação** nas listagens: `GET /cursos?page=0&size=5&sort=nome,asc` (padrão `size=10`, máximo 100). O `sort` aceita: cursos `id, nome, cargaHoraria, categoria, ativo`; alunos `id, nome, email, idade, telefone`; matrículas `id, dataMatricula, status, aluno.id, aluno.nome, curso.id, curso.nome`. Outro campo retorna `400`. A resposta tem o formato `{"content": [...], "page": {"size", "number", "totalElements", "totalPages"}}`.

**Exemplo de cadastro de matrícula** (com os dados de exemplo, o aluno 2 ainda não está no curso 2; repetir o comando retorna `409`, pois já existe matrícula ativa)
```bash
curl -X POST http://localhost:8080/matriculas -H "Content-Type: application/json" \
  -d '{"dataMatricula":"2026-04-01","status":"ATIVA","alunoId":2,"cursoId":2}'
```

**Exemplo de erro de validação (400)**
```json
{"title":"Bad Request","status":400,"detail":"Erro de validação nos campos informados.",
 "invalid_params":{"email":"O e-mail informado é inválido","idade":"A idade mínima é 16 anos"}}
```

## 🛠️ Tecnologias e Ferramentas

* **Linguagem:** Java 21
* **Framework:** Spring Boot 3.3.x (Web, Validation, Data JPA)
* **Banco de dados:** H2 em memória
* **Build Tool:** Maven (Wrapper)
* **Produtividade:** Lombok
* **Documentação:** Springdoc OpenAPI (Swagger UI)
* **Qualidade:** JUnit 5, Mockito, AssertJ e MockMvc
* **Infraestrutura:** Docker e Docker Compose (com Hot-Reload para Dev)

## 🚀 Como Executar

### Opção 1 — Maven (requer Java 21)
```bash
./mvnw spring-boot:run
```

### Opção 2 — Docker (com Live Reload)
```bash
docker compose up -d
```

A API sobe na porta `8080` com dados de exemplo (4 cursos, sendo 1 inativo; 4 alunos; 4 matrículas):

* **Swagger UI (contrato interativo):** http://localhost:8080/
* **Console do H2:** http://localhost:8080/h2-console — JDBC URL `jdbc:h2:mem:matriculas`, usuário `sa`, senha vazia

### Opção 3 — Nuvem (DigitalOcean + Terraform)
Para testar sem instalar nada localmente, veja o passo a passo em [`infra/digitalocean`](infra/digitalocean/README.md) (SDR-018).

### E-mail de confirmação (opcional)
Ao realizar uma matrícula a API pode enviar um e-mail de confirmação pelo [Resend](https://resend.com) (SDR-019). Desligado por padrão; para ligar:
```bash
RESEND_API_KEY=re_xxx RESEND_DESTINO_FIXO=seu@email.com ./mvnw spring-boot:run
```
No modo de teste do Resend, `RESEND_DESTINO_FIXO` deve ser o e-mail da sua conta Resend.

### Testes
```bash
./mvnw clean test
```

## 📚 Documentação

* Documentação online (Swagger UI): 👉 **[Acessar ao vivo](https://carloskvasir.github.io/api_rest_spring_2/)**
* Decisões de arquitetura (SDRs), enunciado, design e plano de implementação: pasta [`/dev-docs/`](dev-docs/README.md). Resumo da execução e dos resultados: [SDR-014](dev-docs/SDRs/SDR-014-planejamento-execucao-resultados.md).
* Histórico de mudanças: [CHANGELOG.md](CHANGELOG.md).

## 📜 Licença

Este projeto está licenciado sob a **Mozilla Public License Version 2.0 (MPL-2.0)**.
Copyright (c) 2026 Carlos Kvasir - [carloskvasir.dev](https://carloskvasir.dev)

Veja o arquivo [LICENSE](LICENSE) para mais detalhes sobre as permissões e limitações.
