output "ip" {
  description = "IP publico do servidor."
  value       = digitalocean_droplet.api.ipv4_address
}

output "swagger_ui" {
  description = "Contrato interativo (disponivel 3 a 6 minutos apos o apply, tempo de compilar a imagem)."
  value       = "http://${digitalocean_droplet.api.ipv4_address}/"
}

output "exemplo_curl" {
  value = "curl http://${digitalocean_droplet.api.ipv4_address}/cursos"
}
