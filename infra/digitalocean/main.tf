locals {
  nome = "api-matriculas"
}

resource "digitalocean_ssh_key" "acesso" {
  count      = var.ssh_public_key == "" ? 0 : 1
  name       = "${local.nome}-key"
  public_key = var.ssh_public_key
}

resource "digitalocean_droplet" "api" {
  name       = local.nome
  region     = var.region
  size       = var.droplet_size
  image      = "ubuntu-24-04-x64"
  ssh_keys   = digitalocean_ssh_key.acesso[*].fingerprint
  monitoring = true
  tags       = [local.nome]

  # Instala o Docker, clona o repositorio, constroi a imagem de producao (Dockerfile) e sobe o container.
  user_data = templatefile("${path.module}/cloud-init.yaml.tftpl", {
    repo_url = var.repo_url
    repo_ref = var.repo_ref
    env_file = join("\n", [
      "RESEND_API_KEY=${var.resend_api_key}",
      "RESEND_FROM=${var.resend_from}",
      "RESEND_DESTINO_FIXO=${var.resend_destino_fixo}",
    ])
  })
}

# Somente HTTP (80) e, opcionalmente, SSH. O console do H2 fica desligado na imagem publicada.
resource "digitalocean_firewall" "api" {
  name        = "${local.nome}-fw"
  droplet_ids = [digitalocean_droplet.api.id]

  inbound_rule {
    protocol         = "tcp"
    port_range       = "80"
    source_addresses = ["0.0.0.0/0", "::/0"]
  }

  dynamic "inbound_rule" {
    for_each = var.ssh_public_key == "" ? [] : [1]
    content {
      protocol         = "tcp"
      port_range       = "22"
      source_addresses = var.ssh_allowed_cidrs
    }
  }

  outbound_rule {
    protocol              = "tcp"
    port_range            = "1-65535"
    destination_addresses = ["0.0.0.0/0", "::/0"]
  }

  outbound_rule {
    protocol              = "udp"
    port_range            = "53"
    destination_addresses = ["0.0.0.0/0", "::/0"]
  }
}

# Alertas por e-mail (e-mail informado em alert_email)
resource "digitalocean_monitor_alert" "cpu" {
  description = "${local.nome}: CPU acima de 80%"
  type        = "v1/insights/droplet/cpu"
  compare     = "GreaterThan"
  value       = 80
  window      = "5m"
  enabled     = true
  entities    = [digitalocean_droplet.api.id]

  alerts {
    email = [var.alert_email]
  }
}

resource "digitalocean_monitor_alert" "memoria" {
  description = "${local.nome}: memoria acima de 90%"
  type        = "v1/insights/droplet/memory_utilization_percent"
  compare     = "GreaterThan"
  value       = 90
  window      = "5m"
  enabled     = true
  entities    = [digitalocean_droplet.api.id]

  alerts {
    email = [var.alert_email]
  }
}
