package com.api.matriculas.controllers;

import com.api.matriculas.dtos.CursoRequestDTO;
import com.api.matriculas.repositories.CursoRepository;
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
class CursoControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CursoRepository cursoRepository;
    @Autowired private MatriculaRepository matriculaRepository;

    @BeforeEach
    void limpar() {
        matriculaRepository.deleteAll();
        cursoRepository.deleteAll();
    }

    private String json(CursoRequestDTO dto) throws Exception {
        return objectMapper.writeValueAsString(dto);
    }

    private String criar(String nome) throws Exception {
        String resposta = mockMvc.perform(post("/cursos").contentType(MediaType.APPLICATION_JSON)
                        .content(json(new CursoRequestDTO(nome, 40, "Backend", true))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("id").asText();
    }

    @Test
    void deveCadastrarCursoERetornar201ComLocation() throws Exception {
        mockMvc.perform(post("/cursos").contentType(MediaType.APPLICATION_JSON)
                        .content(json(new CursoRequestDTO("Java Avancado", 60, "Programacao", true))))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.nome", is("Java Avancado")))
                .andExpect(jsonPath("$.ativo", is(true)));
    }

    @Test
    void nomeCurtoDeveRetornar400ComMensagem() throws Exception {
        mockMvc.perform(post("/cursos").contentType(MediaType.APPLICATION_JSON)
                        .content(json(new CursoRequestDTO("abc", 60, "Programacao", true))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.invalid_params.nome", containsString("5 caracteres")));
    }

    @Test
    void cargaHorariaZeroOuNulaDeveRetornar400() throws Exception {
        mockMvc.perform(post("/cursos").contentType(MediaType.APPLICATION_JSON)
                        .content(json(new CursoRequestDTO("Java Avancado", 0, "Programacao", true))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.invalid_params.cargaHoraria", notNullValue()));

        mockMvc.perform(post("/cursos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Java Avancado\",\"categoria\":\"X\",\"ativo\":true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.invalid_params.cargaHoraria", notNullValue()));
    }

    @Test
    void categoriaEAtivoObrigatorios() throws Exception {
        mockMvc.perform(post("/cursos").contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Java Avancado\",\"cargaHoraria\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.invalid_params.categoria", notNullValue()))
                .andExpect(jsonPath("$.invalid_params.ativo", notNullValue()));
    }

    @Test
    void listagemDeveSerPaginadaEOrdenada() throws Exception {
        criar("Curso Beta");
        criar("Curso Alfa");
        criar("Curso Gama");

        mockMvc.perform(get("/cursos").param("page", "0").param("size", "2").param("sort", "nome,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].nome", is("Curso Alfa")))
                .andExpect(jsonPath("$.content[1].nome", is("Curso Beta")))
                .andExpect(jsonPath("$.page.totalElements", is(3)))
                .andExpect(jsonPath("$.page.totalPages", is(2)));
    }

    @Test
    void sortPorCampoInexistenteDeveRetornar400() throws Exception {
        mockMvc.perform(get("/cursos").param("sort", "campoInexistente,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Bad Request")));
    }

    @Test
    void paginacaoComParametrosAbsurdosNaoDeveRetornar500() throws Exception {
        mockMvc.perform(get("/cursos").param("size", "0")).andExpect(status().is2xxSuccessful());
        mockMvc.perform(get("/cursos").param("page", "-1")).andExpect(status().is2xxSuccessful());
        mockMvc.perform(get("/cursos").param("size", "100000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.size", lessThanOrEqualTo(100)));
        mockMvc.perform(get("/cursos").param("page", "abc")).andExpect(status().is2xxSuccessful());
    }

    @Test
    void paginaComOffsetAcimaDeIntegerMaxDeveRetornar400ProblemDetail() throws Exception {
        for (String rota : new String[]{"/cursos", "/alunos", "/matriculas"}) {
            mockMvc.perform(get(rota).param("page", "2147483647").param("size", "100"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title", is("Bad Request")))
                    .andExpect(jsonPath("$.trace").doesNotExist());
        }
    }

    @Test
    void buscarPorIdDeveRetornarCurso() throws Exception {
        String id = criar("Java Avancado");

        mockMvc.perform(get("/cursos/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Java Avancado")));
    }

    @Test
    void idInexistenteDeveRetornar404NosTresVerbos() throws Exception {
        mockMvc.perform(get("/cursos/9999")).andExpect(status().isNotFound());
        mockMvc.perform(put("/cursos/9999").contentType(MediaType.APPLICATION_JSON)
                        .content(json(new CursoRequestDTO("Java Avancado", 60, "Programacao", true))))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/cursos/9999")).andExpect(status().isNotFound());
    }

    @Test
    void deveAtualizarCurso() throws Exception {
        String id = criar("Java Avancado");

        mockMvc.perform(put("/cursos/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(json(new CursoRequestDTO("Kotlin Basico", 20, "Mobile", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Kotlin Basico")))
                .andExpect(jsonPath("$.ativo", is(false)));
    }

    @Test
    void removerDeveRetornar204EDepoisGetRetorna404() throws Exception {
        String id = criar("Java Avancado");

        mockMvc.perform(delete("/cursos/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/cursos/" + id)).andExpect(status().isNotFound());
        mockMvc.perform(get("/cursos")).andExpect(jsonPath("$.content", hasSize(0)));
    }
}
