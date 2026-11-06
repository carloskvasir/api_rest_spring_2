# SDR-019: E-mail de confirmação de matrícula via Resend

**Status:** Aceito
**Data:** 2026-11-06

## 1. Contexto e Problema
Para o teste em nuvem (SDR-018) a pessoa avaliadora precisa ver um efeito concreto fora da API. O enunciado não pede e-mail; por isso o recurso é opcional, barato e não pode afetar as regras nem a resposta das requisições.

## 2. Decisão
- **Provedor:** Resend (API HTTP simples, plano gratuito; escolha do usuário). Chamada com `RestClient` do Spring, sem nova dependência.
- **Evento:** `MatriculaService.realizar` publica `MatriculaRealizadaEvent`; `NotificacaoMatriculaListener` o consome com `@TransactionalEventListener(AFTER_COMMIT)` e `@Async`. Matrícula rejeitada (422/409/404) ou revertida **não** gera e-mail, e a latência da requisição não muda.
- **Porta de saída:** interface `EmailNotificador` (troca fácil de provedor); implementação `ResendEmailNotificador`.
- **Configuração por ambiente:** `RESEND_API_KEY` (vazia = envio desligado), `RESEND_FROM` (padrão `Matriculas <onboarding@resend.dev>`), `RESEND_DESTINO_FIXO`.
- **Modo de teste do Resend:** o remetente `onboarding@resend.dev` só entrega ao e-mail dono da conta. Com `RESEND_DESTINO_FIXO` todas as confirmações vão para esse endereço; com um domínio verificado em `RESEND_FROM` o aluno recebe diretamente.
- **Terraform:** `resend_api_key` (sensível), `resend_destino_fixo` e `resend_from` viram `/opt/api-matriculas/.env` (0600) no servidor, lido com `docker run --env-file`.

## 3. System Design Review (Análise de Impacto)
- **Segurança:** a chave nunca fica no repositório nem em log; fica no `.env` do servidor e no estado do Terraform (não compartilhar o `terraform.tfstate`). O e-mail usa texto simples, sem HTML, evitando injeção de marcação com nomes de aluno.
- **Resiliência:** falha, lentidão ou chave inválida do Resend são capturadas e registradas (`WARN`); nunca propagam erro ao cliente.
- **Riscos:** sem fila nem retentativa, um e-mail pode ser perdido se o provedor falhar ou a aplicação reiniciar logo após o commit (aceitável para demonstração). O e-mail do aluno é dado pessoal enviado a um terceiro.

## 4. Consequências
- **Positivos:** efeito visível no teste em nuvem, sem alterar o contrato da API; sem dependências novas.
- **Negativos:** `MatriculaService` passa a depender de `ApplicationEventPublisher`. Evolução possível: fila/outbox com retentativa.

## 5. Verificação
110 testes passando, incluindo `ResendEmailNotificadorTest` (MockRestServiceServer: cabeçalho, payload, destino fixo, sem chave, falha do provedor) e `NotificacaoMatriculaIntegrationTest` (envia após commit; não envia em 422). **Envio real ao Resend não foi testado** (sem chave); o `terraform validate` e a renderização do cloud-init passaram.
