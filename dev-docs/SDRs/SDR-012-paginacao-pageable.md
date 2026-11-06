# SDR-012: Paginação com `Pageable`

**Data:** 2026-11-05
**Status:** Aceito

## 1. Contexto e Motivação
O enunciado exige paginação nas listagens com `page`, `size` e `sort` (ex.: `GET /cursos?page=0&size=5&sort=nome,asc`). A paginação manual por `subList` (SDR-002) não ordena e não consulta o banco paginado.

## 2. Decisão
- Controllers recebem `Pageable` e devolvem `Page<...ResponseDTO>` (`Page.map(...::fromEntity)`); Services repassam ao `findAll(Pageable)`.
- `@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)` (classe `PaginacaoConfig`) gera um JSON estável: `{"content": [...], "page": {"size", "number", "totalElements", "totalPages"}}`, evitando o aviso do Spring Data 3.3 sobre a serialização direta de `PageImpl`.
- Limites: padrão **10** itens, máximo **100**. Configurados por `PageableHandlerMethodArgumentResolverCustomizer`, pois com `@EnableSpringDataWebSupport` as propriedades `spring.data.web.pageable.*` do Boot deixam de ser aplicadas (descoberto por teste: `size=100000` retornava 100000).
- `sort` aceita apenas **propriedades permitidas** por recurso (`OrdenacaoPermitida`); qualquer outra, inclusive campo inexistente, campo interno (`deleted`) ou coleção (`matriculas.status`), retorna **400** em `ProblemDetail` listando as permitidas. Ordenar por coleção gera join e **repetia itens entre páginas** (achado da revisão final).
  - Cursos: `id`, `nome`, `cargaHoraria`, `categoria`, `ativo`. Alunos: `id`, `nome`, `email`, `idade`, `telefone`. Matrículas: `id`, `dataMatricula`, `status`, `aluno.id`, `aluno.nome`, `curso.id`, `curso.nome` (associações to-one não duplicam linhas).
- O `id` é acrescentado como **critério de desempate** (`OrdenacaoPermitida.comDesempate`): sem ele, ordenar por campo com valores repetidos deixa a ordem dos empatados indefinida e itens podem repetir ou sumir entre páginas.
- `size=0`, `page=-1` ou `page=abc` caem nos valores padrão.

## 3. System Design Review (Análise de Impacto)
- **Desempenho e Escalabilidade:** limite máximo evita respostas gigantes; paginação executada no banco.
- **Arquitetura:** contrato JSON de página é público; mudar de formato é breaking change.
- **Riscos:** novos campos só ficam ordenáveis ao entrarem na lista `ORDENAVEIS` do Service correspondente.

## 4. Consequências
- **Positivos:** atende ao enunciado; ordenação por qualquer campo; erro claro para `sort` inválido.
- **Negativos:** clientes precisam ler `page.totalElements` em vez de campos na raiz.
