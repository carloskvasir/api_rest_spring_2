package com.api.matriculas.config;

import com.api.matriculas.exceptions.GlobalExceptionHandler;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.valves.ErrorReportValve;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * Requisicoes recusadas pelo Tomcat antes de chegar ao Spring (ex: /cursos/%zz) recebiam a pagina HTML padrao.
 * Este valve do host as converte em ProblemDetail (RFC 7807), sem expor versao do servidor (SDR-017).
 */
@Configuration
public class ErroTomcatConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> problemDetailNoTomcat() {
        return factory -> factory.addContextCustomizers((Context context) -> {
            if (context.getParent() instanceof StandardHost host) {
                host.setErrorReportValveClass(ProblemDetailErrorValve.class.getName());
            }
        });
    }

    public static class ProblemDetailErrorValve extends ErrorReportValve {
        @Override
        protected void report(Request request, Response response, Throwable throwable) {
            int codigo = response.getStatus();
            HttpStatus status = HttpStatus.resolve(codigo);
            if (status == null || codigo < 400 || response.getContentWritten() > 0) {
                return;
            }
            String slug = status.name().toLowerCase().replace('_', '-');
            String corpo = "{\"type\":\"" + GlobalExceptionHandler.BASE_URI + slug + "\","
                    + "\"title\":\"" + status.getReasonPhrase() + "\",\"status\":" + codigo + ","
                    + "\"detail\":\"" + (status.is4xxClientError()
                        ? "A requisição não pôde ser processada."
                        : "Ocorreu um erro interno. Tente novamente mais tarde.") + "\","
                    + "\"timestamp\":\"" + Instant.now() + "\"}";
            try {
                response.setContentType("application/problem+json");
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                response.getOutputStream().write(corpo.getBytes(StandardCharsets.UTF_8));
            } catch (IOException ignorado) {
                // cliente desconectou; nada a fazer
            }
        }
    }
}
