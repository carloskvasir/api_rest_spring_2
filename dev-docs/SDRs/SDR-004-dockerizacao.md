# SDR-004: Dockerização para Facilitação do Desenvolvimento

**Data:** 2026-10-01
**Status:** Aceito

## 1. Contexto e Motivação
Embora o gerenciador \`mise\` resolva a versão do Java localmente, ambientes de desenvolvimento e pipelines de CI/CD precisam de uma forma hermética e universal de empacotar e executar a aplicação. Adicionalmente, caso o sistema evolua e passe a utilizar bancos de dados (ex: PostgreSQL) e cache (ex: Redis), o Docker facilita essa orquestração.

## 2. Decisão
- Utilizar **Docker** com um `Dockerfile` *multi-stage build* baseado em Alpine Linux (para manter a imagem enxuta e segura).
- Criar um **docker-compose.yml** orquestrando o serviço da API, simplificando a inicialização do ambiente de desenvolvimento.

## 3. System Design Review (Análise de Impacto)
- **Arquitetura e Integração:** Arquitetura pronta para a introdução de novos componentes (containers de banco de dados podem ser adicionados ao compose com facilidade). Elimina a síndrome "na minha máquina funciona".
- **Segurança:** O uso da estratégia *multi-stage* garante que o código fonte e as ferramentas de build (JDK, Maven) não sejam enviadas para produção. A imagem final contém estritamente o necessário (JRE + JAR compilado).
- **Desempenho (Build):** O comando `dependency:go-offline` isola o download das dependências em uma camada de cache do Docker, acelerando os builds subsequentes, desde que o `pom.xml` não seja alterado.

## 4. Consequências
- **Positivos:** Onboarding de novos desenvolvedores fica reduzido a um único comando: \`docker compose up -d --build\`. Facilita futuros deploys em infraestruturas cloud (AWS ECS, Kubernetes).
- **Negativos:** Adiciona a necessidade de ter o Docker/Docker Desktop instalado na máquina de desenvolvimento.
