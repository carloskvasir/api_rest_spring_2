package com.api.matriculas.notificacoes;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Envio via API HTTP do Resend (https://resend.com/docs/api-reference/emails/send-email).
 * Sem RESEND_API_KEY o envio fica desligado. Com RESEND_DESTINO_FIXO todos os e-mails vao para esse endereco,
 * necessario no modo de teste do Resend (remetente onboarding@resend.dev so entrega ao dono da conta).
 */
@Slf4j
@Component
public class ResendEmailNotificador implements EmailNotificador {

    private final RestClient cliente;
    private final String chave;
    private final String remetente;
    private final String destinoFixo;

    public ResendEmailNotificador(RestClient.Builder builder,
                                  @Value("${resend.url:https://api.resend.com}") String url,
                                  @Value("${resend.api-key:}") String chave,
                                  @Value("${resend.from:Matriculas <onboarding@resend.dev>}") String remetente,
                                  @Value("${resend.destino-fixo:}") String destinoFixo) {
        this.cliente = builder.baseUrl(url).build();
        this.chave = chave;
        this.remetente = remetente;
        this.destinoFixo = destinoFixo;
    }

    @Override
    public void enviar(String para, String assunto, String texto) {
        if (chave == null || chave.isBlank()) {
            log.debug("Envio de e-mail desligado (RESEND_API_KEY ausente); ignorando: {}", assunto);
            return;
        }
        String destino = destinoFixo == null || destinoFixo.isBlank() ? para : destinoFixo;
        try {
            cliente.post().uri("/emails")
                    .header("Authorization", "Bearer " + chave)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("from", remetente, "to", List.of(destino), "subject", assunto, "text", texto))
                    .retrieve().toBodilessEntity();
            log.info("E-mail enviado via Resend: {}", assunto);
        } catch (Exception e) {
            log.warn("Falha ao enviar e-mail via Resend ({}): {}", assunto, e.getMessage());
        }
    }
}
