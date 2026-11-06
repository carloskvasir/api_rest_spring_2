package com.api.matriculas.notificacoes;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ResendEmailNotificadorTest {

    private static final String URL = "https://api.resend.com";
    private static final String FROM = "Matriculas <onboarding@resend.dev>";

    private MockRestServiceServer servidor;

    private ResendEmailNotificador criar(String chave, String destinoFixo) {
        RestClient.Builder builder = RestClient.builder();
        servidor = MockRestServiceServer.bindTo(builder).build();
        return new ResendEmailNotificador(builder, URL, chave, FROM, destinoFixo);
    }

    @Test
    void deveEnviarParaApiDoResendComChaveEPayload() {
        ResendEmailNotificador notificador = criar("re_teste", "");
        servidor.expect(requestTo(URL + "/emails"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer re_teste"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.from").value(FROM))
                .andExpect(jsonPath("$.to[0]").value("maria@x.com"))
                .andExpect(jsonPath("$.subject").value("Assunto"))
                .andExpect(jsonPath("$.text").value("Corpo"))
                .andRespond(withSuccess("{\"id\":\"1\"}", MediaType.APPLICATION_JSON));

        notificador.enviar("maria@x.com", "Assunto", "Corpo");

        servidor.verify();
    }

    @Test
    void destinoFixoDeveSubstituirODestinatario() {
        ResendEmailNotificador notificador = criar("re_teste", "avaliador@x.com");
        servidor.expect(requestTo(URL + "/emails"))
                .andExpect(jsonPath("$.to[0]").value("avaliador@x.com"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        notificador.enviar("maria@x.com", "Assunto", "Corpo");

        servidor.verify();
    }

    @Test
    void semChaveNaoDeveChamarNenhumServico() {
        ResendEmailNotificador notificador = criar("", "");

        notificador.enviar("maria@x.com", "Assunto", "Corpo");

        servidor.verify(); // nenhuma requisicao esperada nem feita
    }

    @Test
    void falhaDoProvedorNaoDevePropagarExcecao() {
        ResendEmailNotificador notificador = criar("re_teste", "");
        servidor.expect(requestTo(URL + "/emails")).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatCode(() -> notificador.enviar("maria@x.com", "Assunto", "Corpo")).doesNotThrowAnyException();
    }
}
