package com.api.matriculas.services;

import com.api.matriculas.dtos.CursoRequestDTO;
import com.api.matriculas.exceptions.ConflictException;
import com.api.matriculas.exceptions.ResourceNotFoundException;
import com.api.matriculas.models.Curso;
import com.api.matriculas.repositories.CursoRepository;
import com.api.matriculas.repositories.MatriculaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CursoServiceTest {

    @Mock private CursoRepository cursoRepository;
    @Mock private MatriculaRepository matriculaRepository;
    @InjectMocks private CursoService cursoService;

    private Curso curso(Long id) {
        Curso c = new Curso();
        c.setId(id);
        c.setNome("Java Avancado");
        c.setCargaHoraria(60);
        c.setCategoria("Programacao");
        c.setAtivo(true);
        return c;
    }

    @Test
    void deveSalvarCursoMapeandoOsCamposDoDto() {
        when(cursoRepository.save(any(Curso.class))).thenAnswer(i -> i.getArgument(0));

        Curso salvo = cursoService.salvar(new CursoRequestDTO("Spring Boot", 40, "Backend", true));

        assertThat(salvo.getNome()).isEqualTo("Spring Boot");
        assertThat(salvo.getCargaHoraria()).isEqualTo(40);
        assertThat(salvo.getAtivo()).isTrue();
    }

    @Test
    void buscarPorIdInexistenteDeveLancarNotFound() {
        when(cursoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cursoService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Curso não encontrado com id: 99");
    }

    @Test
    void removerCursoComMatriculasDeveLancarConflict() {
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso(1L)));
        when(matriculaRepository.existsByCursoId(1L)).thenReturn(true);

        assertThatThrownBy(() -> cursoService.remover(1L)).isInstanceOf(ConflictException.class);
        verify(cursoRepository, never()).delete(any());
    }

    @Test
    void removerCursoSemMatriculasDeveExcluir() {
        Curso c = curso(1L);
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(c));
        when(matriculaRepository.existsByCursoId(1L)).thenReturn(false);

        cursoService.remover(1L);

        verify(cursoRepository).delete(c);
    }

    @Test
    void atualizarDeveSubstituirTodosOsCampos() {
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso(1L)));
        when(cursoRepository.save(any(Curso.class))).thenAnswer(i -> i.getArgument(0));

        Curso atualizado = cursoService.atualizar(1L, new CursoRequestDTO("Kotlin Basico", 20, "Mobile", false));

        assertThat(atualizado.getNome()).isEqualTo("Kotlin Basico");
        assertThat(atualizado.getAtivo()).isFalse();
    }
}
