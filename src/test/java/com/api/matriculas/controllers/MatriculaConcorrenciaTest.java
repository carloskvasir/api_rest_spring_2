package com.api.matriculas.controllers;

import com.api.matriculas.models.Aluno;
import com.api.matriculas.models.Curso;
import com.api.matriculas.models.StatusMatricula;
import com.api.matriculas.repositories.AlunoRepository;
import com.api.matriculas.repositories.CursoRepository;
import com.api.matriculas.repositories.MatriculaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
class MatriculaConcorrenciaTest {

    @Autowired private MockMvc mockMvc;
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
        aluno = novoAluno();
        curso = novoCurso();
    }

    private Aluno novoAluno() {
        Aluno a = new Aluno();
        a.setNome("Maria");
        a.setEmail("maria@email.com");
        a.setIdade(20);
        return alunoRepository.save(a);
    }

    private Curso novoCurso() {
        Curso c = new Curso();
        c.setNome("Java Avancado");
        c.setCargaHoraria(60);
        c.setCategoria("Programacao");
        c.setAtivo(true);
        return cursoRepository.save(c);
    }

    private int postarMatricula(Long alunoId, Long cursoId) throws Exception {
        return mockMvc.perform(post("/matriculas").contentType(MediaType.APPLICATION_JSON)
                .content("{\"dataMatricula\":\"2026-05-01\",\"status\":\"ATIVA\",\"alunoId\":%d,\"cursoId\":%d}"
                        .formatted(alunoId, cursoId)))
                .andReturn().getResponse().getStatus();
    }

    private List<Integer> executarEmParalelo(List<Callable<Integer>> tarefas) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(tarefas.size());
        CountDownLatch largada = new CountDownLatch(1);
        List<Future<Integer>> futuros = new ArrayList<>();
        for (Callable<Integer> tarefa : tarefas) {
            futuros.add(pool.submit(() -> {
                largada.await();
                return tarefa.call();
            }));
        }
        largada.countDown();
        List<Integer> resultados = new ArrayList<>();
        for (Future<Integer> f : futuros) {
            resultados.add(f.get());
        }
        pool.shutdown();
        return resultados;
    }

    @Test
    void matriculasAtivasSimultaneasDoMesmoParNaoPodemDuplicar() throws Exception {
        List<Callable<Integer>> tarefas = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            tarefas.add(() -> postarMatricula(aluno.getId(), curso.getId()));
        }

        List<Integer> status = executarEmParalelo(tarefas);

        assertThat(status).filteredOn(s -> s == 201).hasSize(1);
        assertThat(status).filteredOn(s -> s == 409).hasSize(19);
        assertThat(matriculaRepository.existsByAlunoIdAndCursoIdAndStatus(
                aluno.getId(), curso.getId(), StatusMatricula.ATIVA)).isTrue();
        assertThat(matriculaRepository.count()).isEqualTo(1);
    }

    @Test
    void removerAlunoEMatricularSimultaneamenteNaoDeixaMatriculaOrfa() throws Exception {
        for (int rodada = 0; rodada < 10; rodada++) {
            matriculaRepository.deleteAll();
            alunoRepository.deleteAll();
            Aluno alvo = novoAluno();

            List<Integer> status = executarEmParalelo(List.of(
                    () -> postarMatricula(alvo.getId(), curso.getId()),
                    () -> mockMvc.perform(delete("/alunos/" + alvo.getId())).andReturn().getResponse().getStatus()));

            boolean matriculou = status.get(0) == 201;
            boolean removeu = status.get(1) == 204;
            assertThat(matriculou && removeu)
                    .as("rodada %d: aluno removido com matricula criada (%s)", rodada, status)
                    .isFalse();
        }
    }
}
