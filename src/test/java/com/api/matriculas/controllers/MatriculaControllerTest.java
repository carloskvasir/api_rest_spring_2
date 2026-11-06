package com.api.matriculas.controllers;

import com.api.matriculas.models.Aluno;
import com.api.matriculas.models.Curso;
import com.api.matriculas.repositories.AlunoRepository;
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
class MatriculaControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private AlunoRepository alunoRepository;
    @Autowired private CursoRepository cursoRepository;
    @Autowired private MatriculaRepository matriculaRepository;

    private Aluno aluno;
    private Curso curso;

    @BeforeEach
    void preparar() {
        matriculaRepository.deleteAll();
        alunoRepository.deleteAll();
        cursoRepository.deleteAll();
        aluno = novoAluno("Maria", "maria@email.com");
        curso = novoCurso("Java Avancado", true);
    }

    private Aluno novoAluno(String nome, String email) {
        Aluno a = new Aluno();
        a.setNome(nome);
        a.setEmail(email);
        a.setIdade(20);
        return alunoRepository.save(a);
    }

    private Curso novoCurso(String nome, boolean ativo) {
        Curso c = new Curso();
        c.setNome(nome);
        c.setCargaHoraria(60);
        c.setCategoria("Programacao");
        c.setAtivo(ativo);
        return cursoRepository.save(c);
    }

    private String corpo(Long alunoId, Long cursoId, String status) {
        return """
            {"dataMatricula":"2026-03-01","status":"%s","alunoId":%d,"cursoId":%d}
            """.formatted(status, alunoId, cursoId);
    }

    private String matricular(Long alunoId, Long cursoId) throws Exception {
        String resposta = mockMvc.perform(post("/matriculas").contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(alunoId, cursoId, "ATIVA")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("id").asText();
    }

    @Test
    void deveRealizarMatriculaERetornar201ComLocation() throws Exception {
        mockMvc.perform(post("/matriculas").contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(aluno.getId(), curso.getId(), "ATIVA")))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status", is("ATIVA")))
                .andExpect(jsonPath("$.dataMatricula", is("2026-03-01")))
                .andExpect(jsonPath("$.aluno.nome", is("Maria")))
                .andExpect(jsonPath("$.curso.nome", is("Java Avancado")));
    }

    @Test
    void alunoOuCursoInexistenteDeveRetornar404() throws Exception {
        mockMvc.perform(post("/matriculas").contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(9999L, curso.getId(), "ATIVA")))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/matriculas").contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(aluno.getId(), 9999L, "ATIVA")))
                .andExpect(status().isNotFound());
    }

    @Test
    void alunoOuCursoApagadosDevemRetornar404() throws Exception {
        Aluno apagado = novoAluno("Joao", "joao@email.com");
        alunoRepository.delete(apagado);
        Curso cursoApagado = novoCurso("Curso Antigo", true);
        cursoRepository.delete(cursoApagado);

        mockMvc.perform(post("/matriculas").contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(apagado.getId(), curso.getId(), "ATIVA")))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/matriculas").contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(aluno.getId(), cursoApagado.getId(), "ATIVA")))
                .andExpect(status().isNotFound());
    }

    @Test
    void cursoInativoDeveRetornar422() throws Exception {
        Curso inativo = novoCurso("Curso Inativo", false);

        mockMvc.perform(post("/matriculas").contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(aluno.getId(), inativo.getId(), "ATIVA")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail", containsString("inativo")));
    }

    @Test
    void matriculaAtivaDuplicadaDeveRetornar409ERefazerAposCancelarPermite() throws Exception {
        String id = matricular(aluno.getId(), curso.getId());

        mockMvc.perform(post("/matriculas").contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(aluno.getId(), curso.getId(), "ATIVA")))
                .andExpect(status().isConflict());

        mockMvc.perform(delete("/matriculas/" + id)).andExpect(status().isNoContent());

        matricular(aluno.getId(), curso.getId());
    }

    @Test
    void camposObrigatoriosEStatusInvalido() throws Exception {
        mockMvc.perform(post("/matriculas").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.invalid_params.dataMatricula", notNullValue()))
                .andExpect(jsonPath("$.invalid_params.status", notNullValue()))
                .andExpect(jsonPath("$.invalid_params.alunoId", notNullValue()))
                .andExpect(jsonPath("$.invalid_params.cursoId", notNullValue()));

        mockMvc.perform(post("/matriculas").contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(aluno.getId(), curso.getId(), "XPTO")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Bad Request")));
    }

    @Test
    void deveAtualizarApenasOStatus() throws Exception {
        String id = matricular(aluno.getId(), curso.getId());

        mockMvc.perform(put("/matriculas/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CONCLUIDA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CONCLUIDA")))
                .andExpect(jsonPath("$.aluno.nome", is("Maria")));

        mockMvc.perform(put("/matriculas/" + id).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reativarMatriculaEmCursoDesativadoDeveRetornar422() throws Exception {
        String id = matricular(aluno.getId(), curso.getId());
        mockMvc.perform(delete("/matriculas/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(put("/cursos/" + curso.getId()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Java Avancado\",\"cargaHoraria\":60,\"categoria\":\"Programacao\",\"ativo\":false}"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/matriculas/" + id).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"ATIVA\"}"))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(get("/matriculas/" + id)).andExpect(jsonPath("$.status", is("CANCELADA")));
    }

    @Test
    void deleteDeveCancelarMantendoORegistro() throws Exception {
        String id = matricular(aluno.getId(), curso.getId());

        mockMvc.perform(delete("/matriculas/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(delete("/matriculas/" + id)).andExpect(status().isNoContent());

        mockMvc.perform(get("/matriculas/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELADA")));
    }

    @Test
    void idInexistenteDeveRetornar404() throws Exception {
        mockMvc.perform(get("/matriculas/9999")).andExpect(status().isNotFound());
        mockMvc.perform(put("/matriculas/9999").contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"ATIVA\"}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/matriculas/9999")).andExpect(status().isNotFound());
    }

    @Test
    void listagemDeveSerPaginadaEOrdenada() throws Exception {
        Aluno outro = novoAluno("Joao", "joao@email.com");
        matricular(aluno.getId(), curso.getId());
        matricular(outro.getId(), curso.getId());

        mockMvc.perform(get("/matriculas").param("page", "0").param("size", "1").param("sort", "id,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].aluno.nome", is("Joao")))
                .andExpect(jsonPath("$.page.totalElements", is(2)))
                .andExpect(jsonPath("$.page.totalPages", is(2)));
    }

    @Test
    void excluirCursoOuAlunoComMatriculaDeveRetornar409() throws Exception {
        matricular(aluno.getId(), curso.getId());

        mockMvc.perform(delete("/cursos/" + curso.getId())).andExpect(status().isConflict());
        mockMvc.perform(delete("/alunos/" + aluno.getId())).andExpect(status().isConflict());
        mockMvc.perform(get("/cursos/" + curso.getId())).andExpect(status().isOk());
    }
}
