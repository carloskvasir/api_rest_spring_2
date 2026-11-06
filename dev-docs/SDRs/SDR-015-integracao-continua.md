# SDR-015: Integração Contínua (GitHub Actions)

**Data:** 2026-11-05
**Status:** Aceito

## 1. Contexto e Motivação
O projeto só tinha um workflow para Gitea (`.gitea/workflows/spring-ci.yml`) com dois defeitos: usava **Java 17**, enquanto o projeto exige **Java 21**, e executava `mvn` em vez do Maven Wrapper (`./mvnw`) versionado. O repositório é hospedado no GitHub, onde esse workflow não roda. Como o `GEMINI.md` exige `./mvnw clean test` verde antes de qualquer commit, a esteira precisa aplicar a mesma regra a cada push e Pull Request.

## 2. Decisão
- Criar `.github/workflows/ci.yml` (GitHub Actions): em `push` na `main` e em todo `pull_request`, faz checkout, configura **Temurin 21** com cache Maven e executa `./mvnw -B clean test`. Em caso de falha, publica `target/surefire-reports/` como artefato. Permissões mínimas (`contents: read`).
- Corrigir o workflow do Gitea para Java 21 e `./mvnw`, mantendo-o para o ambiente local.
- Os testes usam H2 em memória, portanto a esteira não precisa de serviços externos.

## 3. System Design Review (Análise de Impacto)
- **Arquitetura e Integração:** usa as mesmas versões e o mesmo comando do desenvolvimento local, evitando "funciona na minha máquina".
- **Segurança:** sem segredos e com permissão somente leitura; actions oficiais (`actions/*`) fixadas em versão maior (`checkout@v5` e `setup-java@v5`).
- **Desempenho:** cache de dependências Maven reduz o tempo dos builds seguintes.
- **Riscos:** o workflow só pode ser validado de fato após o primeiro push no GitHub; a sintaxe YAML foi validada localmente e o comando de build foi executado localmente.

## 4. Consequências
- **Positivos:** todo push e PR é compilado e testado automaticamente; regressões são detectadas cedo.
- **Negativos:** não há deploy automático nem publicação do GitHub Pages (`docs/`), que continuam manuais (SDR-007).
