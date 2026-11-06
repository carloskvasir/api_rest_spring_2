package com.api.matriculas.notificacoes;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** A matricula realizada dispara o e-mail de confirmacao somente depois do commit (SDR-019). */
@SpringBootTest
@AutoConfigureMockMvc
class NotificacaoMatriculaIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private EmailNotificador notificador;

    @Test
    void matriculaCriadaDeveEnviarEmailParaOAluno() throws Exception {
        String aluno = mockMvc.perform(post("/alunos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Joana\",\"email\":\"joana@x.com\",\"idade\":22}"))
                .andReturn().getResponse().getContentAsString();
        Long alunoId = Long.valueOf(aluno.replaceAll(".*\"id\":(\\d+).*", "$1"));

        mockMvc.perform(post("/matriculas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dataMatricula\":\"2026-05-01\",\"status\":\"ATIVA\",\"alunoId\":" + alunoId + ",\"cursoId\":1}"))
                .andExpect(status().isCreated());

        ArgumentCaptor<String> texto = ArgumentCaptor.forClass(String.class);
        verify(notificador, timeout(3000)).enviar(eq("joana@x.com"), anyString(), texto.capture());
        assertThat(texto.getValue()).contains("Joana");
    }

    @Test
    void matriculaRejeitadaNaoDeveEnviarEmail() throws Exception {
        // curso 4 e o inativo do seed (422)
        mockMvc.perform(post("/matriculas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dataMatricula\":\"2026-05-01\",\"status\":\"ATIVA\",\"alunoId\":1,\"cursoId\":4}"))
                .andExpect(status().isUnprocessableEntity());

        verify(notificador, after(500).never()).enviar(anyString(), anyString(), anyString());
    }
}
