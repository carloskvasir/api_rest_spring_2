# SDR-001: Escolha da Stack Tecnológica Base

**Data:** 2026-10-01
**Status:** Aceito

## 1. Contexto
O projeto requer o desenvolvimento de uma API REST simples para Controle de Livros (Trabalho 1). Precisávamos definir a versão do Java, gerenciador de dependências, arquitetura e padrão de versionamento.

## 2. Decisão
A stack tecnológica e os padrões escolhidos foram:
- **Linguagem e Framework:** Java 21 (LTS) e Spring Boot 3.x.
- **Gerenciador de Dependências:** Maven (com wrapper `mvnw`).
- **Gerenciador de Ambiente:** `mise` configurado apenas para instalar o Java via `.mise.toml`.
- **Arquitetura:** Padrão clássico em camadas (`models`, `dtos`, `services`, `controllers`, `exceptions`).
- **Armazenamento:** Inicialmente em memória utilizando coleções do próprio Java (`List`).

## 3. Consequências
- **Positivos:** Uso de tecnologias modernas (Java 21) mantendo a estabilidade (LTS). Arquitetura familiar e de fácil manutenção para microsserviços Spring. Ausência de dependências complexas (banco de dados real) facilita a execução imediata por qualquer desenvolvedor.
- **Negativos:** O armazenamento em memória será perdido a cada reinício da aplicação (o que é aceitável para o escopo do Trabalho 1).
