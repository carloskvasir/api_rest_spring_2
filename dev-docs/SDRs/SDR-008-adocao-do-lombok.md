# SDR-008: Adoção do Lombok e Template de Pull Requests

**Data:** 2026-10-01
**Status:** Aceito

## 1. Contexto e Motivação
Havia um volume desnecessário de código boilerplate (Getters, Setters e Construtores) nas Entidades e Services (`Livro.java`, `LivroController`). Além disso, o projeto não possuía um padrão de Pull Request para garantir que as alterações enviadas por outros desenvolvedores cobrissem os requisitos de testes e design.

## 2. Decisão
1. Adicionar o **Lombok** via Maven para gerar código repetitivo em tempo de compilação. Entidades passam a utilizar `@Data`, e injeções de dependência em construtores utilizarão `@RequiredArgsConstructor`.
2. Criar um **Pull Request Template** (`.github/pull_request_template.md`) exigindo o preenchimento de checklists (testes e atualizações de Changelog) para manter a governança forte do repositório.

## 3. Consequências
- Código muito mais conciso e focado nas regras de negócio em vez de infraestrutura do Java.
- Processo de revisão de código formalizado através do PR Template.
