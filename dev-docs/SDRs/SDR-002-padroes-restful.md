# SDR-002: Adequação a Padrões RESTful Avançados

**Data:** 2026-10-01
**Status:** Aceito

## 1. Contexto e Motivação
A implementação inicial da API atendia todos os requisitos do trabalho (GET, POST, PUT, DELETE), mas carecia de maturidade REST (RFCs e Richardson Maturity Model). O objetivo é evoluir a API para padrões de mercado.

## 2. Decisão
Implementamos 4 atualizações:
1. **Cabeçalho Location:** Adicionado ao `201 Created` no POST usando `ServletUriComponentsBuilder`.
2. **ProblemDetail (RFC 7807):** Migração do formato JSON de erros simples para `ProblemDetail`, o novo padrão no Spring Boot 3.
3. **Paginação Suave:** Inserção de `page` e `size` no método GET via `@RequestParam`, mantendo o retorno como um Array JSON simples para não quebrar testes baseados no formato da especificação.
4. **Endpoint PATCH:** Criação de `atualizarParcial` recebendo um `Map<String, Object>` para atualizar campos avulsos.
*Nota:* HATEOAS não foi implementado pois alteraria drasticamente a estrutura JSON de saída, o que romperia o contrato explícito ("Exemplos esperados") do PDF do trabalho.

## 3. System Design Review (Análise de Impacto)
- **Arquitetura e Integração:** Melhoria drástica na integração de clientes. O cliente agora sabe onde o recurso foi criado via cabeçalho e obtém erros semânticos uniformes (RFC 7807).
- **Segurança:** O uso rigoroso do ProblemDetail previne vazamento de stacktraces.
- **Desempenho e Escalabilidade:** A paginação (mesmo em memória) simula boas práticas que previnem alto consumo de banda (OutOfMemory).

## 4. Consequências
- **Positivos:** A API evoluiu para o Nível 2 de Richardson completo, mais maduro e alinhado a RFCs reais.
- **Negativos:** Complexidade adicional no serviço para gerenciar reflection manual/conversão de Map no `PATCH`.
