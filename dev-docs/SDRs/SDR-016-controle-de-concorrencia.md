# SDR-016: Controle de concorrência com lock pessimista

**Data:** 2026-11-05
**Status:** Aceito

## 1. Contexto e Motivação
A revisão final apontou uma condição de corrida: a regra "no máximo uma matrícula `ATIVA` por aluno e curso" era verificada com *check-then-insert* sem lock. Reproduzido com testes: 20 `POST /matriculas` simultâneos criaram **6 matrículas ativas** para o mesmo par; e `DELETE /alunos/{id}` concorrente com `POST /matriculas` deixou um aluno removido com matrícula criada (`[201, 204]`). Exclusões também poderiam violar a regra "não excluir com matrículas".

## 2. Decisão
- `AlunoRepository` e `CursoRepository` ganham `buscarParaEscrita(id)` com `@Lock(PESSIMISTIC_WRITE)` (`SELECT ... FOR UPDATE`); os Services expõem `buscarParaEscrita`, que respeita o `@SoftDelete` (removido → 404).
- `MatriculaService.realizar` e a reativação em `atualizarStatus` bloqueiam **aluno e depois curso**; `AlunoService.remover` e `CursoService.remover` bloqueiam o próprio registro antes de checar `existsBy...`. A ordem fixa aluno → curso e o fato de cada exclusão travar só um registro evitam deadlock.
- A URL do H2 ganhou `LOCK_TIMEOUT=10000` para que requisições concorrentes aguardem o lock em vez de falhar por timeout (padrão do H2: 2 s).
- Alternativas descartadas: constraint única parcial (`status = ATIVA`; o H2 não suporta índice parcial portável); `@Version` (otimista, exigiria retentativas e não cobre a exclusão); `synchronized` (não funciona com mais de uma instância).

## 3. System Design Review (Análise de Impacto)
- **Arquitetura e Integração:** locks acontecem dentro da transação do Service; Controllers e DTOs não mudam.
- **Desempenho e Escalabilidade:** operações de escrita no mesmo aluno ou curso são serializadas; leituras não são afetadas. Adequado ao escopo.
- **Riscos:** novas operações que travem mais de um registro devem respeitar a ordem aluno → curso.

## 4. Consequências
- **Positivos:** a regra de matrícula única e as exclusões ficam corretas sob concorrência, comprovado por `MatriculaConcorrenciaTest` (vermelho antes, verde depois, 6 execuções estáveis) e por 20 `curl` paralelos na aplicação real (1×201 e 19×409, três rodadas).
- **Negativos:** um pouco mais de latência sob disputa no mesmo registro; testes de concorrência são mais lentos.
