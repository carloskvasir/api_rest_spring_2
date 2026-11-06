package com.api.matriculas.config;

import com.api.matriculas.models.StatusMatricula;
import com.api.matriculas.repositories.AlunoRepository;
import com.api.matriculas.repositories.CursoRepository;
import com.api.matriculas.repositories.MatriculaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

// Banco H2 proprio: os demais testes limpam as tabelas do banco padrao.
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:seedtest;DB_CLOSE_DELAY=-1")
class DataSeederTest {

    @Autowired private CursoRepository cursoRepository;
    @Autowired private AlunoRepository alunoRepository;
    @Autowired private MatriculaRepository matriculaRepository;

    @Test
    void deveSemearCursosAlunosEMatriculas() {
        assertThat(cursoRepository.count()).isGreaterThanOrEqualTo(4);
        assertThat(alunoRepository.count()).isGreaterThanOrEqualTo(4);
        assertThat(matriculaRepository.count()).isGreaterThanOrEqualTo(3);
    }

    @Test
    void deveIncluirAoMenosUmCursoInativoParaDemonstracao() {
        assertThat(cursoRepository.findAll()).anyMatch(c -> Boolean.FALSE.equals(c.getAtivo()));
    }

    @Test
    void todosOsAlunosSemeadosSaoMaioresDe16() {
        assertThat(alunoRepository.findAll()).allMatch(a -> a.getIdade() >= 16);
    }

    @Test
    void deveSemearMatriculasAtivas() {
        assertThat(matriculaRepository.findAll()).anyMatch(m -> m.getStatus() == StatusMatricula.ATIVA);
    }
}
