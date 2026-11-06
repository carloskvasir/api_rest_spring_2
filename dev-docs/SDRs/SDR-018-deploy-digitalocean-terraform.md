# SDR-018: Deploy na DigitalOcean com Terraform

**Status:** Aceito
**Data:** 2026-11-06

## 1. Contexto e Problema
Quem avalia o trabalho precisa testar a API sem instalar Java nem Docker localmente. O objetivo é um deploy rápido, reproduzível e sem credenciais no repositório, que uma pessoa com conta nova na nuvem consiga executar.

## 2. Decisão
- **Provedor:** DigitalOcean (escolha do usuário; a alternativa GCP exige projeto, faturamento e APIs antes do Terraform).
- **Infraestrutura:** um droplet `s-1vcpu-2gb` + firewall (80, e 22 opcional) + 2 alertas de monitoramento por e-mail, em `infra/digitalocean/`.
- **Execução:** `cloud-init` instala Docker, clona o repositório público, constrói o `Dockerfile` (imagem de produção) e roda o container em `-p 80:8080`, com swap de 2 GB para a compilação.
- **E-mail:** a aplicação não tem login nem envio de e-mail; o e-mail informado em `alert_email` serve apenas aos alertas. Quem avalia cria a própria conta e gera o token (passo a passo em `infra/digitalocean/README.md`).
- **Segredos:** `do_token` é variável `sensitive`; `terraform.tfvars` e o estado ficam no `.gitignore`.

## 3. System Design Review (Análise de Impacto)
- **Segurança:** console do H2 desligado no deploy (`SPRING_H2_CONSOLE_ENABLED=false`); só a porta 80 é pública. O tráfego é HTTP sem TLS, aceitável só para demonstração com dados de exemplo.
- **Custo:** cobrança por hora enquanto o droplet existir; `terraform destroy` encerra.
- **Riscos:** a imagem é compilada no servidor (3 a 6 minutos); o estado do Terraform é local; dados em H2 em memória se perdem ao reiniciar.

## 4. Consequências
- **Positivos:** deploy em poucos comandos, reproduzível e descartável.
- **Negativos:** sem HTTPS, domínio nem banco persistente. Evoluções possíveis: Caddy com Let's Encrypt, imagem pré-construída em registro, estado remoto.

## 5. Verificação
`terraform fmt` e `terraform validate` executados (Terraform 1.9, imagem oficial). **O `terraform apply` não foi executado**: exige token e gera custo, por isso o deploy real ainda não foi testado.
