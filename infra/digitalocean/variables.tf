variable "do_token" {
  description = "Token da API da DigitalOcean (Control Panel > API > Generate New Token, com permissao de escrita)."
  type        = string
  sensitive   = true
}

variable "alert_email" {
  description = "E-mail que recebe os alertas de monitoramento do servidor (CPU e memoria)."
  type        = string
}

variable "region" {
  description = "Regiao do droplet (ex: nyc3, sfo3, ams3, fra1, blr1)."
  type        = string
  default     = "nyc3"
}

variable "droplet_size" {
  description = "Tamanho do droplet. 2 GB de RAM e o minimo confortavel para compilar o projeto com Maven."
  type        = string
  default     = "s-1vcpu-2gb"
}

variable "repo_url" {
  description = "Repositorio publico que sera clonado e executado."
  type        = string
  default     = "https://github.com/carloskvasir/api_rest_spring_2.git"
}

variable "repo_ref" {
  description = "Branch ou tag a executar."
  type        = string
  default     = "main"
}

variable "ssh_public_key" {
  description = "Chave publica SSH opcional, para acessar o servidor. Sem ela o acesso SSH fica indisponivel."
  type        = string
  default     = ""
}

variable "ssh_allowed_cidrs" {
  description = "Enderecos autorizados na porta 22 (so vale se ssh_public_key for informada)."
  type        = list(string)
  default     = ["0.0.0.0/0", "::/0"]
}
