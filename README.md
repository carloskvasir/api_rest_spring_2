# API REST de Controle de Livros

> 🔗 **Repositório Oficial no GitHub:** [carloskvasir/api_rest_spring](https://github.com/carloskvasir/api_rest_spring)
> _(Caso esteja avaliando este projeto a partir de um backup .zip, utilize o link acima para acessar o código online atualizado)._

Este projeto é uma API RESTful avançada desenvolvida em **Java + Spring Boot** para gerenciar um cadastro de livros, seguindo rigorosos padrões RESTful (RFC 7807, cabeçalhos Location, PATCH parcial) e arquitetura validada por SDRs (System Design Reviews). O armazenamento dos dados é realizado em memória.

## 👥 Alunos
* Carlos Kvasir Lima
* David Marlon

## 🛠️ Tecnologias e Ferramentas

* **Linguagem:** Java 21
* **Framework:** Spring Boot 3.3.x
* **Build Tool:** Maven (Wrapper)
* **Produtividade:** Lombok (Reducao de Boilerplate)
* **Qualidade:** Testes Unitários e de Integração (JUnit 5 + MockMvc)
* **Infraestrutura:** Docker e Docker Compose (com Hot-Reload mapeado para Dev)

## 🚀 Como Executar (Ambiente de Desenvolvimento)

O ambiente foi totalmente dockerizado com **Live Reload**. Qualquer alteração feita e salva no código da sua IDE será refletida automaticamente na API em menos de 1 segundo.

1. Certifique-se de ter o Docker instalado e rodando.
2. Na raiz do projeto, suba a aplicação em background:
   ```bash
   docker compose up -d
   ```
3. A API estará disponível na porta `8080`: **http://localhost:8080/**

## 📚 Endpoints e Documentação

Para facilitar os testes (via Postman, Insomnia ou Navegador), **acesse a raiz da aplicação (`http://localhost:8080/`)**. 
A raiz da aplicação redirecionará você para a maravilhosa interface visual do Swagger UI, contendo o contrato OpenAPI detalhado e executável de todos os endpoints, seus métodos HTTP e exemplos de payloads esperados para cadastro e atualização.

As documentações das decisões arquiteturais (SDRs) e padrões da IA (GEMINI.md) estão armazenadas na pasta `/dev-docs/`.

## 📜 Licença

Este projeto está licenciado sob a **Mozilla Public License Version 2.0 (MPL-2.0)**.
Copyright (c) 2026 Carlos Kvasir - [carloskvasir.dev](https://carloskvasir.dev)

Veja o arquivo [LICENSE](LICENSE) para mais detalhes sobre as permissões e limitações.

## 🌐 Documentação Online (Swagger UI)

A documentação interativa e os contratos da API estão hospedados publicamente através do GitHub Pages. Você pode interagir com a interface (ler schemas, descobrir payloads e formatos esperados) sem precisar executar o projeto localmente:

👉 **[Acessar a Documentação da API ao vivo (Swagger Online)](https://carloskvasir.github.io/api_rest_spring/)**
