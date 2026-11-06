package com.api.matriculas;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.ByteArrayOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/** URL malformada e recusada pelo Tomcat antes do Spring MVC; a resposta tambem deve ser ProblemDetail (SDR-017). */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ErroTomcatTest {

    @LocalServerPort private int porta;

    @Test
    void urlMalformadaDeveRetornarProblemDetailEmVezDeHtml() throws Exception {
        String resposta = enviar("GET /cursos/%zz HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n");

        assertThat(resposta).startsWith("HTTP/1.1 400");
        assertThat(resposta.toLowerCase()).contains("content-type: application/problem+json");
        assertThat(resposta).contains("\"status\":400")
                .contains("\"type\":\"https://api.matriculas.com/errors/bad-request\"")
                .contains("\"timestamp\"")
                .doesNotContain("<html").doesNotContain("Tomcat").doesNotContain("trace");
    }

    private String enviar(String requisicao) throws Exception {
        try (Socket socket = new Socket("localhost", porta)) {
            socket.getOutputStream().write(requisicao.getBytes(StandardCharsets.US_ASCII));
            socket.getOutputStream().flush();
            ByteArrayOutputStream saida = new ByteArrayOutputStream();
            socket.getInputStream().transferTo(saida);
            return saida.toString(StandardCharsets.UTF_8);
        }
    }
}
