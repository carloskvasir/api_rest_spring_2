# SDR-003: Adoção de Cultura de Testes e Qualidade

**Data:** 2026-10-01
**Status:** Aceito

## 1. Contexto e Motivação
Garantir o funcionamento contínuo do sistema, atestar que as regras de negócio foram implementadas perfeitamente e blindar a API RESTful (validações, HTTP status) contra problemas em evoluções futuras.

## 2. Decisão
- Forçar a IA através de regras do `GEMINI.md` a atuar com rigor de "linter" e aplicar testes sistematicamente.
- Desenvolver **Testes Unitários** para as regras de negócio (`LivroServiceTest`).
- Desenvolver **Testes de Integração** para os comportamentos HTTP e de Bean Validation (`LivroControllerTest`).

## 3. System Design Review (Análise de Impacto)
- **Arquitetura e Integração:** Não interfere no tempo de inicialização de produção, apenas exige recursos na esteira de CI/CD. MockMvc viabiliza testes rápidos dos endpoints.
- **Riscos e Segurança:** Testes detectam imediatamente se regras de validação (como evitar preços negativos) caírem por um erro humano. O risco é mitigado brutalmente.
- **Desempenho:** Testes sem banco de dados (armazenamento em memória) rodam em poucos segundos, gerando feedback instantâneo ao desenvolvedor.

## 4. Consequências
- **Positivos:** Facilidade em refatorar o código amanhã sem perder o sono; documentação clara e base de qualidade.
- **Negativos:** Nenhum efeito colateral sistêmico. Exige mais código a ser mantido.
