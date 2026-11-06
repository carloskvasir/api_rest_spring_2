package com.api.matriculas.config;

import com.api.matriculas.dtos.AlunoRequestDTO;
import com.api.matriculas.dtos.CursoRequestDTO;
import com.api.matriculas.dtos.MatriculaRequestDTO;
import com.api.matriculas.models.Aluno;
import com.api.matriculas.models.Curso;
import com.api.matriculas.models.StatusMatricula;
import com.api.matriculas.services.AlunoService;
import com.api.matriculas.services.CursoService;
import com.api.matriculas.services.MatriculaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CursoService cursoService;
    private final AlunoService alunoService;
    private final MatriculaService matriculaService;

    @Override
    public void run(String... args) {
        log.info("🌱 Semeando dados iniciais (Seeds)...");

        Curso java = cursoService.salvar(new CursoRequestDTO("Java com Spring Boot", 60, "Programação", true));
        Curso banco = cursoService.salvar(new CursoRequestDTO("Banco de Dados Relacionais", 40, "Dados", true));
        Curso web = cursoService.salvar(new CursoRequestDTO("Desenvolvimento Web", 80, "Programação", true));
        cursoService.salvar(new CursoRequestDTO("Delphi Legado", 30, "Programação", false));

        Aluno ana = alunoService.salvar(new AlunoRequestDTO("Ana Souza", "ana.souza@email.com", 22, "11988880001"));
        Aluno bruno = alunoService.salvar(new AlunoRequestDTO("Bruno Lima", "bruno.lima@email.com", 19, null));
        Aluno carla = alunoService.salvar(new AlunoRequestDTO("Carla Dias", "carla.dias@email.com", 17, "21977770003"));
        alunoService.salvar(new AlunoRequestDTO("Diego Alves", "diego.alves@email.com", 30, null));

        matricular(ana, java, StatusMatricula.ATIVA, LocalDate.of(2026, 2, 1));
        matricular(ana, banco, StatusMatricula.CONCLUIDA, LocalDate.of(2025, 8, 15));
        matricular(bruno, java, StatusMatricula.ATIVA, LocalDate.of(2026, 2, 3));
        matricular(carla, web, StatusMatricula.TRANCADA, LocalDate.of(2026, 3, 10));

        log.info("🌱 Seeds concluídos: 4 cursos, 4 alunos e 4 matrículas.");
    }

    private void matricular(Aluno aluno, Curso curso, StatusMatricula status, LocalDate data) {
        matriculaService.realizar(new MatriculaRequestDTO(data, status, aluno.getId(), curso.getId()));
    }
}
