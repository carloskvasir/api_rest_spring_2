# SDR-009: Gerador de Scaffolds (Boilerplate Maker)

**Data:** 2026-10-01
**Status:** Aceito

## 1. Contexto e Motivação
Toda vez que uma nova entidade de negócio (ex: `Autor`, `Categoria`, `Editora`) precisa ser desenvolvida, o time gasta preciosos minutos recriando as exatas mesmas 5 camadas do sistema: `Model` (com Lombok), `RequestDTO` (Records), `ResponseDTO` (Records), `Service` (com tratamentos padrões) e `Controller` (com injeções e cabeçalhos Location padronizados).

## 2. Decisão
Implementar um script local (`bin/scaffold.sh`) para atuar como um "Gerador" de infraestrutura, semelhante aos geradores do Ruby on Rails ou NestJS. Esse gerador embute nativamente as escolhas tecnológicas estritas deste projeto: Records, Injeções Lombok via `@RequiredArgsConstructor`, respostas seguindo RFC de erros via arquitetura padronizada.

## 3. Consequências
- Aceleração drástica do desenvolvimento (basta digitar `./bin/scaffold.sh Entidade`).
- Evita erros humanos ao esquecer anotações essenciais (`@Valid`, `@RestController`).
- Padronização arquitetural imaculada.
