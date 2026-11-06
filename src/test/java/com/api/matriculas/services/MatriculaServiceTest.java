package com.api.matriculas.services;

import com.api.matriculas.dtos.MatriculaRequestDTO;
import com.api.matriculas.exceptions.BusinessRuleException;
import com.api.matriculas.exceptions.ConflictException;
import com.api.matriculas.exceptions.ResourceNotFoundException;
import com.api.matriculas.models.Aluno;
import com.api.matriculas.models.Curso;
import com.api.matriculas.models.Matricula;
import com.api.matriculas.models.StatusMatricula;
import com.api.matriculas.repositories.MatriculaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatriculaServiceTest {

    @Mock private MatriculaRepository matriculaRepository;
    @Mock private AlunoService alunoService;
    @Mock private CursoService cursoService;
    @InjectMocks private MatriculaService matriculaService;

    private Aluno aluno;
    private Curso curso;

    @BeforeEach
    void preparar() {
        aluno = new Aluno();
        aluno.setId(1L);
        aluno.setNome("Maria");
        aluno.setIdade(20);
        curso = new Curso();
        curso.setId(2L);
        curso.setNome("Java Avancado");
        curso.setAtivo(true);
    }

    private MatriculaRequestDTO dto(StatusMatricula status) {
        return new MatriculaRequestDTO(LocalDate.of(2026, 3, 1), status, 1L, 2L);
    }

    private void aluno_e_curso_existem() {
        when(alunoService.buscarParaEscrita(1L)).thenReturn(aluno);
        when(cursoService.buscarParaEscrita(2L)).thenReturn(curso);
    }

    @Test
    void realizarDeveSalvarMatriculaValida() {
        aluno_e_curso_existem();
        when(matriculaRepository.existsByAlunoIdAndCursoIdAndStatus(1L, 2L, StatusMatricula.ATIVA)).thenReturn(false);
        when(matriculaRepository.save(any(Matricula.class))).thenAnswer(i -> i.getArgument(0));

        Matricula m = matriculaService.realizar(dto(StatusMatricula.ATIVA));

        assertThat(m.getAluno()).isSameAs(aluno);
        assertThat(m.getCurso()).isSameAs(curso);
        assertThat(m.getStatus()).isEqualTo(StatusMatricula.ATIVA);
        assertThat(m.getDataMatricula()).isEqualTo(LocalDate.of(2026, 3, 1));
    }

    @Test
    void realizarEmCursoInativoLancaBusinessRule() {
        curso.setAtivo(false);
        aluno_e_curso_existem();

        assertThatThrownBy(() -> matriculaService.realizar(dto(StatusMatricula.ATIVA)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("inativo");
        verify(matriculaRepository, never()).save(any());
    }

    @Test
    void realizarAlunoMenorDe16LancaBusinessRule() {
        aluno.setIdade(15);
        aluno_e_curso_existem();

        assertThatThrownBy(() -> matriculaService.realizar(dto(StatusMatricula.ATIVA)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("16");
        verify(matriculaRepository, never()).save(any());
    }

    @Test
    void realizarDuplicataAtivaLancaConflict() {
        aluno_e_curso_existem();
        when(matriculaRepository.existsByAlunoIdAndCursoIdAndStatus(1L, 2L, StatusMatricula.ATIVA)).thenReturn(true);

        assertThatThrownBy(() -> matriculaService.realizar(dto(StatusMatricula.ATIVA)))
                .isInstanceOf(ConflictException.class);
        verify(matriculaRepository, never()).save(any());
    }

    @Test
    void realizarAposMatriculaCanceladaPermite() {
        aluno_e_curso_existem();
        // so existe matricula CANCELADA: a consulta por ATIVA devolve false
        when(matriculaRepository.existsByAlunoIdAndCursoIdAndStatus(1L, 2L, StatusMatricula.ATIVA)).thenReturn(false);
        when(matriculaRepository.save(any(Matricula.class))).thenAnswer(i -> i.getArgument(0));

        assertThat(matriculaService.realizar(dto(StatusMatricula.ATIVA))).isNotNull();
    }

    @Test
    void realizarComStatusDiferenteDeAtivaNaoChecaDuplicata() {
        aluno_e_curso_existem();
        when(matriculaRepository.save(any(Matricula.class))).thenAnswer(i -> i.getArgument(0));

        matriculaService.realizar(dto(StatusMatricula.CONCLUIDA));

        verify(matriculaRepository, never()).existsByAlunoIdAndCursoIdAndStatus(anyLong(), anyLong(), any());
    }

    @Test
    void alunoInexistentePropaga404() {
        when(alunoService.buscarParaEscrita(1L)).thenThrow(new ResourceNotFoundException("Aluno não encontrado com id: 1"));

        assertThatThrownBy(() -> matriculaService.realizar(dto(StatusMatricula.ATIVA)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private Matricula existente(StatusMatricula status) {
        Matricula m = new Matricula();
        m.setId(10L);
        m.setAluno(aluno);
        m.setCurso(curso);
        m.setStatus(status);
        m.setDataMatricula(LocalDate.of(2026, 1, 1));
        return m;
    }

    @Test
    void atualizarStatusAlteraApenasOStatus() {
        Matricula m = existente(StatusMatricula.ATIVA);
        when(matriculaRepository.findById(10L)).thenReturn(Optional.of(m));
        when(matriculaRepository.save(m)).thenReturn(m);

        Matricula r = matriculaService.atualizarStatus(10L, StatusMatricula.CONCLUIDA);

        assertThat(r.getStatus()).isEqualTo(StatusMatricula.CONCLUIDA);
        assertThat(r.getAluno()).isSameAs(aluno);
        assertThat(r.getDataMatricula()).isEqualTo(LocalDate.of(2026, 1, 1));
    }

    @Test
    void reativarMatriculaQuandoJaExisteOutraAtivaLancaConflict() {
        Matricula m = existente(StatusMatricula.CANCELADA);
        when(matriculaRepository.findById(10L)).thenReturn(Optional.of(m));
        aluno_e_curso_existem();
        when(matriculaRepository.existsByAlunoIdAndCursoIdAndStatus(1L, 2L, StatusMatricula.ATIVA)).thenReturn(true);

        assertThatThrownBy(() -> matriculaService.atualizarStatus(10L, StatusMatricula.ATIVA))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void reativarMatriculaEmCursoInativoLancaBusinessRule() {
        curso.setAtivo(false);
        Matricula m = existente(StatusMatricula.CANCELADA);
        when(matriculaRepository.findById(10L)).thenReturn(Optional.of(m));
        aluno_e_curso_existem();

        assertThatThrownBy(() -> matriculaService.atualizarStatus(10L, StatusMatricula.ATIVA))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("inativo");
        assertThat(m.getStatus()).isEqualTo(StatusMatricula.CANCELADA);
    }

    @Test
    void concluirMatriculaEmCursoInativoContinuaPermitido() {
        curso.setAtivo(false);
        Matricula m = existente(StatusMatricula.ATIVA);
        when(matriculaRepository.findById(10L)).thenReturn(Optional.of(m));
        when(matriculaRepository.save(m)).thenReturn(m);

        assertThat(matriculaService.atualizarStatus(10L, StatusMatricula.CONCLUIDA).getStatus())
                .isEqualTo(StatusMatricula.CONCLUIDA);
    }

    @Test
    void cancelarMudaStatusParaCancelada() {
        Matricula m = existente(StatusMatricula.ATIVA);
        when(matriculaRepository.findById(10L)).thenReturn(Optional.of(m));

        matriculaService.cancelar(10L);

        assertThat(m.getStatus()).isEqualTo(StatusMatricula.CANCELADA);
        verify(matriculaRepository, never()).delete(any());
    }

    @Test
    void cancelarDuasVezesNaoFalha() {
        Matricula m = existente(StatusMatricula.CANCELADA);
        when(matriculaRepository.findById(10L)).thenReturn(Optional.of(m));

        matriculaService.cancelar(10L);

        assertThat(m.getStatus()).isEqualTo(StatusMatricula.CANCELADA);
    }

    @Test
    void buscarInexistenteLancaNotFound() {
        when(matriculaRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matriculaService.buscarPorId(5L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Matrícula não encontrada com id: 5");
    }
}
