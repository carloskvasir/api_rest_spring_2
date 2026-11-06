package com.api.matriculas.repositories;

import com.api.matriculas.models.Aluno;
import com.api.matriculas.models.Curso;
import com.api.matriculas.models.Matricula;
import com.api.matriculas.models.StatusMatricula;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class RepositoriosTest {

    @Autowired private CursoRepository cursoRepository;
    @Autowired private AlunoRepository alunoRepository;
    @Autowired private MatriculaRepository matriculaRepository;
    @Autowired private TestEntityManager em;
    @Autowired private JdbcTemplate jdbc;

    private Curso novoCurso() {
        Curso c = new Curso();
        c.setNome("Java Avancado");
        c.setCargaHoraria(60);
        c.setCategoria("Programacao");
        c.setAtivo(true);
        return cursoRepository.save(c);
    }

    private Aluno novoAluno() {
        Aluno a = new Aluno();
        a.setNome("Maria");
        a.setEmail("maria@email.com");
        a.setIdade(20);
        return alunoRepository.save(a);
    }

    private Matricula novaMatricula(Aluno aluno, Curso curso, StatusMatricula status) {
        Matricula m = new Matricula();
        m.setDataMatricula(LocalDate.of(2026, 1, 10));
        m.setStatus(status);
        m.setAluno(aluno);
        m.setCurso(curso);
        return matriculaRepository.save(m);
    }

    @Test
    void deveSalvarCursoComMatricula() {
        Curso curso = novoCurso();
        Matricula m = novaMatricula(novoAluno(), curso, StatusMatricula.ATIVA);
        em.flush();
        em.clear();

        Matricula salva = matriculaRepository.findById(m.getId()).orElseThrow();
        assertThat(salva.getCurso().getId()).isEqualTo(curso.getId());
        assertThat(salva.getStatus()).isEqualTo(StatusMatricula.ATIVA);
    }

    @Test
    void deleteDeCursoFazSoftDeleteEOcultaDoFindById() {
        Curso curso = novoCurso();
        em.flush();

        cursoRepository.delete(curso);
        em.flush();
        em.clear();

        assertThat(cursoRepository.findById(curso.getId())).isEmpty();
        assertThat(cursoRepository.findAll()).isEmpty();
    }

    @Test
    void deleteDeCursoNaoApagaLinhaFisica() {
        Curso curso = novoCurso();
        em.flush();

        cursoRepository.delete(curso);
        em.flush();

        Boolean deleted = jdbc.queryForObject("select deleted from curso where id = ?", Boolean.class, curso.getId());
        assertThat(deleted).isTrue();
    }

    @Test
    void existsByCursoIdEExistsByAlunoIdDetectamMatricula() {
        Curso curso = novoCurso();
        Aluno aluno = novoAluno();
        assertThat(matriculaRepository.existsByCursoId(curso.getId())).isFalse();
        assertThat(matriculaRepository.existsByAlunoId(aluno.getId())).isFalse();

        novaMatricula(aluno, curso, StatusMatricula.CANCELADA);

        assertThat(matriculaRepository.existsByCursoId(curso.getId())).isTrue();
        assertThat(matriculaRepository.existsByAlunoId(aluno.getId())).isTrue();
    }

    @Test
    void existsByAlunoIdAndCursoIdAndStatusDistingueStatus() {
        Curso curso = novoCurso();
        Aluno aluno = novoAluno();
        novaMatricula(aluno, curso, StatusMatricula.CANCELADA);

        assertThat(matriculaRepository.existsByAlunoIdAndCursoIdAndStatus(
                aluno.getId(), curso.getId(), StatusMatricula.ATIVA)).isFalse();

        novaMatricula(aluno, curso, StatusMatricula.ATIVA);

        assertThat(matriculaRepository.existsByAlunoIdAndCursoIdAndStatus(
                aluno.getId(), curso.getId(), StatusMatricula.ATIVA)).isTrue();
    }

    @Test
    void matriculaSemAlunoFalha() {
        Curso curso = novoCurso();
        Matricula m = new Matricula();
        m.setDataMatricula(LocalDate.now());
        m.setStatus(StatusMatricula.ATIVA);
        m.setCurso(curso);

        assertThatThrownBy(() -> {
            matriculaRepository.save(m);
            em.flush();
        }).isInstanceOfAny(PersistenceException.class, jakarta.validation.ConstraintViolationException.class,
                org.springframework.dao.DataIntegrityViolationException.class);
    }
}
