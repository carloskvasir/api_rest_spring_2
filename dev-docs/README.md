# Documentação de Desenvolvimento (Dev-Docs)

Este diretório armazena a base de conhecimento técnica e arquitetural do projeto. O coração desta documentação são os **Registros de Decisões de Sistema (SDRs)**.

## 📚 Índice de SDRs (System Design Records)

Este índice serve como um mapa rápido para as inteligências artificiais e desenvolvedores compreenderem as decisões fundamentais moldadas na arquitetura do projeto ao longo do tempo.

| SDR | Decisão / Tema | Link Rápido |
| :--- | :--- | :--- |
| **SDR-001** | Definição da Stack Tecnológica Base (Java 21, Spring Boot 3, MVC) | [Acessar Documento](SDRs/SDR-001-stack-tecnologica.md) |
| **SDR-002** | Governança e Maturidade RESTful (RFC 7807, Location, PATCH) | [Acessar Documento](SDRs/SDR-002-padroes-restful.md) |
| **SDR-003** | Cultura de Testes de Integração Obrigatórios (JUnit, MockMvc) | [Acessar Documento](SDRs/SDR-003-testes-qualidade.md) |
| **SDR-004** | Orquestração e Isolamento via Docker (Multi-stage) | [Acessar Documento](SDRs/SDR-004-dockerizacao.md) |
| **SDR-005** | Experiência de Desenvolvimento (Hot-Reload com Volumes Docker) | [Acessar Documento](SDRs/SDR-005-hot-reload-docker.md) |
| **SDR-006** | Contratos Oficiais e Interatividade via Springdoc OpenAPI (Swagger) | [Acessar Documento](SDRs/SDR-006-openapi-swagger.md) |
| **SDR-007** | Automação de Seeds (DataSeeder) e Documentação Estática (GitHub Pages) | [Acessar Documento](SDRs/SDR-007-automacao-dev-e-docs-estaticas.md) |
| **SDR-008** | Adoção do Lombok e Template de Pull Requests | [Acessar Documento](SDRs/SDR-008-adocao-do-lombok.md) |
| **SDR-009** | Gerador de Scaffold e Produtividade | [Acessar Documento](SDRs/SDR-009-gerador-de-scaffold.md) |

---
## 🧠 O Papel dos SDRs no Projeto

> Baseado nos princípios de **System Design Review (SDR)** de Bruno Russo, a criação de SDRs serve como governança para promover a detecção precoce de falhas, melhoria da qualidade da arquitetura, e garantia de escalabilidade. A omissão de documentar mudanças significativas gera débitos técnicos invisíveis.

**Para a IA:** Se precisar tomar qualquer decisão arquitetural nova (como integração de banco de dados, sistema de mensageria, etc.), consulte os SDRs acima para alinhar-se à filosofia do projeto. Após a alteração, copie o modelo `SDRs/SDR-TEMPLATE.md`, gere o novo SDR (ex: `SDR-008`), e atualize IMEDIATAMENTE este índice.
