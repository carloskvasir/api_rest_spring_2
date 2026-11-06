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
import com.api.matriculas.notificacoes.MatriculaRealizadaEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class MatriculaService {

    static final int IDADE_MINIMA = 16;

    static final Set<String> ORDENAVEIS = Set.of("id", "dataMatricula", "status", "aluno.id", "aluno.nome", "curso.id", "curso.nome");

    private final MatriculaRepository matriculaRepository;
    private final AlunoService alunoService;
    private final CursoService cursoService;
    private final ApplicationEventPublisher eventos;

    @Transactional
    public Matricula realizar(MatriculaRequestDTO dto) {
        // Locks sempre na ordem aluno -> curso (evita deadlock); serializam matriculas e exclusoes concorrentes (SDR-016)
        Aluno aluno = alunoService.buscarParaEscrita(dto.alunoId());
        Curso curso = cursoService.buscarParaEscrita(dto.cursoId());

        validarElegibilidade(aluno, curso);
        if (dto.status() == StatusMatricula.ATIVA) {
            validarSemMatriculaAtiva(aluno.getId(), curso.getId());
        }

        Matricula matricula = new Matricula();
        matricula.setDataMatricula(dto.dataMatricula());
        matricula.setStatus(dto.status());
        matricula.setAluno(aluno);
        matricula.setCurso(curso);
        Matricula salva = matriculaRepository.save(matricula);
        eventos.publishEvent(new MatriculaRealizadaEvent(aluno.getEmail(), aluno.getNome(), curso.getNome(), salva.getDataMatricula()));
        return salva;
    }

    @Transactional(readOnly = true)
    public Page<Matricula> listar(Pageable pageable) {
        OrdenacaoPermitida.validar(pageable, ORDENAVEIS);
        return matriculaRepository.findAll(OrdenacaoPermitida.comDesempate(pageable));
    }

    @Transactional(readOnly = true)
    public Matricula buscarPorId(Long id) {
        return matriculaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Matrícula não encontrada com id: " + id));
    }

    @Transactional
    public Matricula atualizarStatus(Long id, StatusMatricula novoStatus) {
        Matricula matricula = buscarPorId(id);
        if (novoStatus == StatusMatricula.ATIVA && matricula.getStatus() != StatusMatricula.ATIVA) {
            Aluno aluno = alunoService.buscarParaEscrita(matricula.getAluno().getId());
            Curso curso = cursoService.buscarParaEscrita(matricula.getCurso().getId());
            validarElegibilidade(aluno, curso);
            validarSemMatriculaAtiva(aluno.getId(), curso.getId());
        }
        matricula.setStatus(novoStatus);
        return matriculaRepository.save(matricula);
    }

    @Transactional
    public void cancelar(Long id) {
        Matricula matricula = buscarPorId(id);
        matricula.setStatus(StatusMatricula.CANCELADA);
    }

    private void validarElegibilidade(Aluno aluno, Curso curso) {
        if (!Boolean.TRUE.equals(curso.getAtivo())) {
            throw new BusinessRuleException("Não é possível matricular em curso inativo: " + curso.getNome());
        }
        if (aluno.getIdade() == null || aluno.getIdade() < IDADE_MINIMA) {
            throw new BusinessRuleException("Aluno menor de " + IDADE_MINIMA + " anos não pode ser matriculado.");
        }
    }

    private void validarSemMatriculaAtiva(Long alunoId, Long cursoId) {
        if (matriculaRepository.existsByAlunoIdAndCursoIdAndStatus(alunoId, cursoId, StatusMatricula.ATIVA)) {
            throw new ConflictException("O aluno já possui uma matrícula ativa neste curso.");
        }
    }
}
