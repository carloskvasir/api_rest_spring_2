# Deploy na DigitalOcean (Terraform)

Sobe a API em um droplet Ubuntu com Docker, firewall e alertas por e-mail. Nenhuma credencial fica no repositório.
Custo aproximado: US$ 12/mês (`s-1vcpu-2gb`), cobrado por hora; destrua ao terminar.

## Passo a passo (quem avalia)

1. **Criar a conta** em https://cloud.digitalocean.com/registrations/new (e-mail, senha e cartão; contas novas costumam ter crédito promocional).
2. **(Opcional) E-mail:** crie a conta gratuita em https://resend.com, gere uma chave em *API Keys* e use-a em `resend_api_key`. No modo de teste do Resend só o e-mail dono da conta recebe mensagens: informe-o em `resend_destino_fixo`. Sem chave a API funciona normalmente, apenas sem enviar e-mail.
3. **Gerar o token:** painel > *API* > *Generate New Token*, com permissão de **escrita**. Copie o valor (aparece uma vez).
4. **Instalar o Terraform** (>= 1.5): https://developer.hashicorp.com/terraform/install
5. **Configurar:**
   ```bash
   cd infra/digitalocean
   cp terraform.tfvars.example terraform.tfvars   # preencha do_token, alert_email e, se quiser, resend_*
   ```
6. **Criar:**
   ```bash
   terraform init
   terraform apply        # confirme com "yes"
   ```
7. **Testar:** aguarde de 3 a 6 minutos (o servidor compila a imagem) e abra a URL do output `swagger_ui` (`http://<ip>/`). Exemplo: `curl http://<ip>/cursos`. Para ver o e-mail, cadastre uma matrícula (`POST /matriculas`); a confirmação chega no endereço configurado.
8. **Destruir (para parar a cobrança):** `terraform destroy`.

## O que é criado
| Recurso | Função |
|---|---|
| `digitalocean_droplet` | Ubuntu 24.04; o cloud-init instala Docker, clona o repositório, constrói o `Dockerfile` e roda o container na porta 80 |
| `digitalocean_firewall` | Só HTTP (80); SSH (22) apenas se `ssh_public_key` for informada |
| `digitalocean_monitor_alert` (2) | E-mail para `alert_email` se CPU > 80% ou memória > 90% por 5 minutos |

## Observações
- A chave do Resend fica em `/opt/api-matriculas/.env` (permissão 0600) no servidor e no estado do Terraform; não compartilhe o `terraform.tfstate`.
- Dados em H2 **em memória**: reiniciar o container volta ao conjunto de exemplo. O console do H2 fica desligado (perfil `prod`).
- É HTTP puro (sem domínio nem certificado), adequado para demonstração. Não publique dados reais.
- Para acompanhar a instalação: informe `ssh_public_key` e leia `/var/log/api-matriculas-deploy.log` no servidor.
- Para publicar outra versão: `terraform apply -var repo_ref=<tag-ou-branch>` recria o droplet.
