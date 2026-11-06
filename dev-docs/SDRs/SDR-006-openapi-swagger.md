# SDR-006: Adoção do Springdoc OpenAPI (Swagger UI)

**Data:** 2026-10-01
**Status:** Aceito

## 1. Contexto e Motivação
Tornar a API padronizada não se restringe apenas aos verbos HTTP e códigos de erro. A forma como os clientes (frontend, outros microsserviços ou avaliadores) descobrem os contratos da API deve seguir um padrão universal.

## 2. Decisão
- Adotar o pacote **Springdoc OpenAPI** (versão 2.x para Spring Boot 3).
- Isso expõe automaticamente a especificação em JSON (`/v3/api-docs`) e uma interface visual iterativa (`/swagger-ui.html`).
- Os metadados de contato foram preenchidos com os dados do proprietário (Carlos Kvasir, carloskvasir.dev) e com a licença MPL 2.0.

## 3. System Design Review (Análise de Impacto)
- **Arquitetura e Integração:** Elimina a necessidade de manter collections de Postman manuais estáticas. Qualquer ferramenta cliente pode gerar stubs a partir do contrato OpenAPI.
- **Segurança e Risco:** A interface fica exposta, o que em produção real poderia necessitar bloqueio no Spring Security, mas para nosso escopo atual e ambiente Docker local é o cenário ideal.
- **Desempenho:** Impacto irrisório na compilação e inicialização.

## 4. Consequências
- **Positivos:** Facilidade imensa de documentação visual (DX) e testes manuais; alinhamento aos padrões massivos de mercado.
- **Negativos:** N/A.
