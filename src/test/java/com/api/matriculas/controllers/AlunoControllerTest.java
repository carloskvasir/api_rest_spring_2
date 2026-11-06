package com.api.matriculas.controllers;

import com.api.matriculas.dtos.AlunoRequestDTO;
import com.api.matriculas.repositories.AlunoRepository;
import com.api.matriculas.repositories.MatriculaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AlunoControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private AlunoRepository alunoRepository;
    @Autowired private MatriculaRepository matriculaRepository;

    @BeforeEach
    void limpar() {
        matriculaRepository.deleteAll();
        alunoRepository.deleteAll();
    }

    private String criar(String nome, String email) throws Exception {
        String resposta = mockMvc.perform(post("/alunos").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AlunoRequestDTO(nome, email, 20, null))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("id").asText();
    }

    @Test
    void deveCadastrarAlunoERetornar201ComLocation() throws Exception {
        mockMvc.perform(post("/alunos").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AlunoRequestDTO("Maria", "maria@email.com", 20, "11999990000"))))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.nome", is("Maria")))
                .andExpect(jsonPath("$.telefone", is("11999990000")));
    }

    @Test
    void telefoneEOpcional() throws Exception {
        mockMvc.perform(post("/alunos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Maria\",\"email\":\"maria@email.com\",\"idade\":20}"))
                .andExpect(status().isCreated());
    }

    @Test
    void emailInvalidoDeveRetornar400ComMensagem() throws Exception {
        mockMvc.perform(post("/alunos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Maria\",\"email\":\"nao-eh-email\",\"idade\":20}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.invalid_params.email", is("O e-mail informado é inválido")));
    }

    @Test
    void idadeMenorQue16OuAusenteDeveRetornar400() throws Exception {
        mockMvc.perform(post("/alunos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Maria\",\"email\":\"maria@email.com\",\"idade\":15}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.invalid_params.idade", is("A idade mínima é 16 anos")));

        mockMvc.perform(post("/alunos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Maria\",\"email\":\"maria@email.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.invalid_params.idade", notNullValue()));
    }

    @Test
    void nomeEmBrancoDeveRetornar400() throws Exception {
        mockMvc.perform(post("/alunos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"  \",\"email\":\"maria@email.com\",\"idade\":20}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.invalid_params.nome", notNullValue()));
    }

    @Test
    void listagemDeveSerPaginadaEOrdenada() throws Exception {
        criar("Carla", "carla@email.com");
        criar("Ana", "ana@email.com");
        criar("Bruno", "bruno@email.com");

        mockMvc.perform(get("/alunos").param("page", "0").param("size", "2").param("sort", "nome,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].nome", is("Ana")))
                .andExpect(jsonPath("$.content[1].nome", is("Bruno")))
                .andExpect(jsonPath("$.page.totalElements", is(3)));
    }

    @Test
    void buscarAtualizarERemover() throws Exception {
        String id = criar("Maria", "maria@email.com");

        mockMvc.perform(get("/alunos/" + id)).andExpect(status().isOk()).andExpect(jsonPath("$.nome", is("Maria")));

        mockMvc.perform(put("/alunos/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AlunoRequestDTO("Maria Silva", "maria@email.com", 21, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Maria Silva")))
                .andExpect(jsonPath("$.idade", is(21)));

        mockMvc.perform(delete("/alunos/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/alunos/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void idInexistenteDeveRetornar404() throws Exception {
        mockMvc.perform(get("/alunos/9999")).andExpect(status().isNotFound());
        mockMvc.perform(delete("/alunos/9999")).andExpect(status().isNotFound());
    }
}
