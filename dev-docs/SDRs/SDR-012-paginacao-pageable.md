# SDR-012: Paginação com `Pageable`

**Data:** 2026-11-05
**Status:** Aceito

## 1. Contexto e Motivação
O enunciado exige paginação nas listagens com `page`, `size` e `sort` (ex.: `GET /cursos?page=0&size=5&sort=nome,asc`). A paginação manual por `subList` (SDR-002) não ordena e não consulta o banco paginado.

## 2. Decisão
- Controllers recebem `Pageable` e devolvem `Page<...ResponseDTO>` (`Page.map(...::fromEntity)`); Services repassam ao `findAll(Pageable)`.
- `@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)` (classe `PaginacaoConfig`) gera um JSON estável: `{"content": [...], "page": {"size", "number", "totalElements", "totalPages"}}`, evitando o aviso do Spring Data 3.3 sobre a serialização direta de `PageImpl`.
- Limites: padrão **10** itens, máximo **100**. Configurados por `PageableHandlerMethodArgumentResolverCustomizer`, pois com `@EnableSpringDataWebSupport` as propriedades `spring.data.web.pageable.*` do Boot deixam de ser aplicadas (descoberto por teste: `size=100000` retornava 100000).
- `sort` por campo inexistente → `PropertyReferenceException` tratada como **400** em `ProblemDetail`; `size=0`, `page=-1` ou `page=abc` caem nos valores padrão.

## 3. System Design Review (Análise de Impacto)
- **Desempenho e Escalabilidade:** limite máximo evita respostas gigantes; paginação executada no banco.
- **Arquitetura:** contrato JSON de página é público; mudar de formato é breaking change.
- **Riscos:** `sort` aceita qualquer propriedade da entidade (inclusive `matriculas` em curso/aluno); aceitável no escopo.

## 4. Consequências
- **Positivos:** atende ao enunciado; ordenação por qualquer campo; erro claro para `sort` inválido.
- **Negativos:** clientes precisam ler `page.totalElements` em vez de campos na raiz.
