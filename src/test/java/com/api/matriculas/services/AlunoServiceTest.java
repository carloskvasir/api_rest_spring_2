package com.api.matriculas.services;

import com.api.matriculas.dtos.AlunoRequestDTO;
import com.api.matriculas.exceptions.ConflictException;
import com.api.matriculas.exceptions.ResourceNotFoundException;
import com.api.matriculas.models.Aluno;
import com.api.matriculas.repositories.AlunoRepository;
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
class AlunoServiceTest {

    @Mock private AlunoRepository alunoRepository;
    @Mock private MatriculaRepository matriculaRepository;
    @InjectMocks private AlunoService alunoService;

    private Aluno aluno(Long id) {
        Aluno a = new Aluno();
        a.setId(id);
        a.setNome("Maria");
        a.setEmail("maria@email.com");
        a.setIdade(20);
        return a;
    }

    @Test
    void deveSalvarAlunoMapeandoOsCamposDoDto() {
        when(alunoRepository.save(any(Aluno.class))).thenAnswer(i -> i.getArgument(0));

        Aluno salvo = alunoService.salvar(new AlunoRequestDTO("Joao", "joao@email.com", 18, "11999990000"));

        assertThat(salvo.getNome()).isEqualTo("Joao");
        assertThat(salvo.getEmail()).isEqualTo("joao@email.com");
        assertThat(salvo.getIdade()).isEqualTo(18);
        assertThat(salvo.getTelefone()).isEqualTo("11999990000");
    }

    @Test
    void buscarPorIdInexistenteDeveLancarNotFound() {
        when(alunoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alunoService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Aluno não encontrado com id: 99");
    }

    @Test
    void removerAlunoComMatriculasDeveLancarConflict() {
        when(alunoRepository.findById(1L)).thenReturn(Optional.of(aluno(1L)));
        when(matriculaRepository.existsByAlunoId(1L)).thenReturn(true);

        assertThatThrownBy(() -> alunoService.remover(1L)).isInstanceOf(ConflictException.class);
        verify(alunoRepository, never()).delete(any());
    }

    @Test
    void removerAlunoSemMatriculasDeveExcluir() {
        Aluno a = aluno(1L);
        when(alunoRepository.findById(1L)).thenReturn(Optional.of(a));
        when(matriculaRepository.existsByAlunoId(1L)).thenReturn(false);

        alunoService.remover(1L);

        verify(alunoRepository).delete(a);
    }
}
