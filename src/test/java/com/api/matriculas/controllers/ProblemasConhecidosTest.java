package com.api.matriculas.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Pendencias conhecidas (SDR-014) corrigidas: coercao de decimal, e-mail sem ponto e erros fora do Spring MVC. */
@SpringBootTest
@AutoConfigureMockMvc
class ProblemasConhecidosTest {

    @Autowired private MockMvc mockMvc;

    @Test
    void idadeDecimalDeveSerRejeitadaEmVezDeTruncada() throws Exception {
        mockMvc.perform(post("/alunos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana\",\"email\":\"ana@x.com\",\"idade\":20.7}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("Corpo da requisição inválido ou malformado.")));
    }

    @Test
    void cargaHorariaDecimalDeveSerRejeitada() throws Exception {
        mockMvc.perform(post("/cursos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Curso Teste\",\"cargaHoraria\":40.5,\"categoria\":\"TI\",\"ativo\":true}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void emailSemPontoNoDominioDeveSerRejeitado() throws Exception {
        mockMvc.perform(post("/alunos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana\",\"email\":\"a@b\",\"idade\":20}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.invalid_params.email", is("O e-mail informado é inválido")));
    }

    @Test
    void emailValidoDeveSerAceito() throws Exception {
        mockMvc.perform(post("/alunos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana\",\"email\":\"ana.silva+x@mail.exemplo.com.br\",\"idade\":20}"))
                .andExpect(status().isCreated());
    }

    @Test
    void rotaDeErroDoContainerDeveRetornarProblemDetail() throws Exception {
        mockMvc.perform(get("/error").requestAttr("jakarta.servlet.error.status_code", 400)
                        .requestAttr("jakarta.servlet.error.request_uri", "/cursos/%zz"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type", is("https://api.matriculas.com/errors/bad-request")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void openApiDeveDocumentarCodigosDeErroEPaginacao() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/cursos'].post.responses['201']", notNullValue()))
                .andExpect(jsonPath("$.paths['/cursos'].post.responses['400']", notNullValue()))
                .andExpect(jsonPath("$.paths['/cursos/{id}'].get.responses['404']", notNullValue()))
                .andExpect(jsonPath("$.paths['/cursos/{id}'].delete.responses['204']", notNullValue()))
                .andExpect(jsonPath("$.paths['/cursos/{id}'].delete.responses['409']", notNullValue()))
                .andExpect(jsonPath("$.paths['/matriculas'].post.responses['422']", notNullValue()))
                .andExpect(jsonPath("$.paths['/matriculas'].post.responses['409']", notNullValue()))
                .andExpect(jsonPath("$.paths['/cursos'].get.parameters[*].name", hasItems("page", "size", "sort")));
    }
}
