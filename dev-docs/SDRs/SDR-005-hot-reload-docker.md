# SDR-005: Configuração de Hot-Reload no Docker para Desenvolvimento

**Data:** 2026-10-01
**Status:** Aceito

## 1. Contexto e Motivação
A dockerização inicial (SDR-004) implementou uma construção hermética (multi-stage) focada na criação de um artefato de produção (.jar). Embora segura, essa abordagem gerou fricção no desenvolvimento, exigindo o comando `docker compose up --build` a cada linha de código alterada. Precisávamos restaurar a agilidade no ambiente de desenvolvimento local (DX - Developer Experience).

## 2. Decisão
- Inclusão da dependência `spring-boot-devtools` no `pom.xml` para habilitar o live-reload nativo do Spring.
- Refatoração do `docker-compose.yml` para apontar exclusivamente para o `target: build` (Estágio 1 do Dockerfile, que contém o JDK e o Maven).
- Mapeamento de volumes locais (`./:/app`) para o interior do container, sincronizando o código-fonte em tempo real sem precisar recompilar a imagem.
- Execução do Spring Boot no modo de desenvolvimento (`./mvnw spring-boot:run`) como `command` no Compose.

## 3. System Design Review (Análise de Impacto)
- **Arquitetura e Integração:** Cria-se uma fronteira clara: O `Dockerfile` puro permanece como meio para gerar a imagem de Produção, enquanto o `docker-compose.yml` se torna uma ferramenta exclusiva de orquestração para Desenvolvimento.
- **Desempenho (Desenvolvimento):** O tempo de iteração do desenvolvedor cai de múltiplos segundos (rebuild de containers) para ~1 segundo (hot-swap do Spring Boot). Um volume nomeado `maven-cache` também foi alocado para evitar downloads redundantes ao recriar o container.

## 4. Consequências
- **Positivos:** Máxima agilidade aliada à padronização hermética do ambiente Docker.
- **Negativos:** O `docker-compose.yml` não poderá ser usado diretamente em servidores de Produção sem a remoção dos volumes locais e do override do `command`.
