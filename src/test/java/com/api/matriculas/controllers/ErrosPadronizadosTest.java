package com.api.matriculas.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Erros gerados pelo proprio Spring MVC (rota, metodo, content-type) tambem saem em ProblemDetail (RFC 7807). */
@SpringBootTest
@AutoConfigureMockMvc
class ErrosPadronizadosTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private Environment env;

    @Test
    void rotaInexistenteDeveRetornar404ProblemDetail() throws Exception {
        mockMvc.perform(get("/naoexiste"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.type", is("https://api.matriculas.com/errors/not-found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void metodoNaoSuportadoDeveRetornar405ProblemDetailComAllow() throws Exception {
        mockMvc.perform(patch("/cursos/1"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().exists("Allow"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title", is("Method Not Allowed")))
                .andExpect(jsonPath("$.type", is("https://api.matriculas.com/errors/method-not-allowed")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));

        mockMvc.perform(delete("/cursos")).andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.title", is("Method Not Allowed")));
    }

    @Test
    void contentTypeNaoSuportadoDeveRetornar415ProblemDetail() throws Exception {
        mockMvc.perform(post("/cursos").contentType(MediaType.TEXT_PLAIN).content("texto"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title", is("Unsupported Media Type")))
                .andExpect(jsonPath("$.type", is("https://api.matriculas.com/errors/unsupported-media-type")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void parametroDePaginaComTipoInvalidoContinuaRetornando400ComFormatoDoProjeto() throws Exception {
        mockMvc.perform(post("/matriculas").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type", is("https://api.matriculas.com/errors/bad-request")))
                .andExpect(jsonPath("$.invalid_params.alunoId", notNullValue()));
    }

    @Test
    void stackTraceENaoDevemVazarNoCorpoDosErros() {
        assertThat(env.getProperty("server.error.include-stacktrace")).isEqualTo("never");
        assertThat(env.getProperty("server.error.include-message")).isEqualTo("never");
        assertThat(env.getProperty("server.error.include-binding-errors")).isEqualTo("never");
    }
}
