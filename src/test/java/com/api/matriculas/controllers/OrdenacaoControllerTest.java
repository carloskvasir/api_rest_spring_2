package com.api.matriculas.controllers;

import com.api.matriculas.repositories.AlunoRepository;
import com.api.matriculas.repositories.CursoRepository;
import com.api.matriculas.repositories.MatriculaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Lista de propriedades ordenaveis (SDR-012): ordenar por colecoes duplicava itens entre paginas. */
@SpringBootTest
@AutoConfigureMockMvc
class OrdenacaoControllerTest {

    @Autowired private MockMvc mockMvc;

    @Test
    void ordenarPorColecaoDeveRetornar400ListandoAsPermitidas() throws Exception {
        for (String rota : List.of("/cursos?sort=matriculas.status", "/cursos?sort=matriculas",
                "/alunos?sort=matriculas.curso.nome,desc", "/matriculas?sort=aluno.matriculas.id")) {
            mockMvc.perform(get(rota))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title", is("Bad Request")))
                    .andExpect(jsonPath("$.detail", containsString("Permitidas")));
        }
    }

    @Test
    void camposInternosOuInexistentesDevemSerRejeitados() throws Exception {
        mockMvc.perform(get("/cursos?sort=deleted")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/alunos?sort=naoExiste")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/cursos?sort=nome&sort=matriculas.id")).andExpect(status().isBadRequest());
    }

    @Test
    void ordenacoesLegitimasContinuamFuncionando() throws Exception {
        for (String rota : List.of("/cursos?sort=nome,asc", "/cursos?sort=cargaHoraria,desc&sort=id",
                "/cursos?sort=categoria", "/cursos?sort=ativo",
                "/alunos?sort=email", "/alunos?sort=idade,desc", "/alunos?sort=telefone",
                "/matriculas?sort=dataMatricula,desc", "/matriculas?sort=status",
                "/matriculas?sort=aluno.nome,asc", "/matriculas?sort=curso.nome,desc")) {
            mockMvc.perform(get(rota)).andExpect(status().isOk());
        }
    }

    @Test
    void semOrdenacaoContinuaFuncionando() throws Exception {
        mockMvc.perform(get("/cursos")).andExpect(status().isOk());
        mockMvc.perform(get("/alunos")).andExpect(status().isOk());
        mockMvc.perform(get("/matriculas")).andExpect(status().isOk());
    }
}
