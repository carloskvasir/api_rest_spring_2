package com.api.matriculas.notificacoes;

/** Porta de saida para envio de e-mail; a implementacao padrao usa a API do Resend (SDR-019). */
public interface EmailNotificador {

    /** Envia um e-mail de texto simples. Nunca lanca excecao: falhas sao registradas em log. */
    void enviar(String para, String assunto, String texto);
}
