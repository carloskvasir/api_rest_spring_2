# Trabalho Prático 2 - Desenvolvimento de API REST com Spring Boot (enunciado original)

## Objetivo
API REST com Spring Boot e persistência em H2, aplicando: CRUD completo; rotas REST (GET, POST, PUT, DELETE); persistência com JPA; relacionamentos entre entidades; Bean Validation; paginação; boas práticas de modelagem e organização.

## Tema
Sistema de Gerenciamento de Cursos e Matrículas: cursos, alunos e matrículas realizadas nos cursos.

## Entidades (campos mínimos)
**Curso**: id (Long, PK); nome (String, obrigatório, mínimo 5 caracteres); cargaHoraria (Integer, obrigatório, > 0); categoria (String, obrigatório); ativo (Boolean, obrigatório).

**Aluno**: id (Long, PK); nome (String, obrigatório); email (String, obrigatório e válido); idade (Integer, mínimo 16 anos); telefone (String, opcional).

**Matrícula**: id (Long, PK); dataMatricula (LocalDate, obrigatório); status (String, obrigatório); aluno (Aluno, relacionamento obrigatório); curso (Curso, relacionamento obrigatório).

## Relacionamentos
Um curso possui várias matrículas; um aluno possui várias matrículas; uma matrícula pertence a um único aluno e a um único curso. Esperados: `@OneToMany` e `@ManyToOne`.

## Funcionalidades obrigatórias
- CRUD de Cursos: cadastrar, listar, buscar por ID, atualizar, remover.
- CRUD de Alunos: cadastrar, listar, buscar por ID, atualizar, remover.
- CRUD de Matrículas: realizar matrícula, listar, buscar por ID, atualizar status, cancelar.

## Paginação
Rotas de listagem com `Pageable` e parâmetros `page`, `size`, `sort`. Ex.: `GET /cursos?page=0&size=5&sort=nome,asc`.

## Validações
Bean Validation nas entidades (`@NotBlank`, `@NotNull`, `@Size`, `@Email`, `@Min`, `@Positive`), com mensagens adequadas de erro.

## Endpoints esperados
- Cursos: `POST /cursos`, `GET /cursos`, `GET /cursos/{id}`, `PUT /cursos/{id}`, `DELETE /cursos/{id}`
- Alunos: `POST /alunos`, `GET /alunos`, `GET /alunos/{id}`, `PUT /alunos/{id}`, `DELETE /alunos/{id}`
- Matrículas: `POST /matriculas`, `GET /matriculas`, `GET /matriculas/{id}`, `PUT /matriculas/{id}`, `DELETE /matriculas/{id}`

## Regras de negócio (a aplicação deve impedir)
- Matrícula em curso inativo
- Matrícula de aluno menor de 16 anos
- Exclusão de curso que possua matrículas
- Exclusão de aluno que possua matrículas

## Organização
Separação em camadas: Controller, Repository, Model/Entity.

## Entrega
Código-fonte completo; projeto funcionando; README com nome do aluno, descrição da API, endpoints disponíveis e como executar.

## Critérios de avaliação (10,0)
Funcionamento do CRUD 2,0; Uso correto de JPA 2,0; Relacionamentos 2,0; Bean Validation 1,5; Paginação 1,0; Organização do código 1,0; Boas práticas REST 0,5.

## Observações
Individual; cópias = nota zero; o sistema deve inicializar sem erros; banco H2; o professor pode solicitar demonstração prática da API.
