package com.api.matriculas.services;

import com.api.matriculas.dtos.CursoRequestDTO;
import com.api.matriculas.exceptions.ConflictException;
import com.api.matriculas.exceptions.ResourceNotFoundException;
import com.api.matriculas.models.Curso;
import com.api.matriculas.repositories.CursoRepository;
import com.api.matriculas.repositories.MatriculaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class CursoService {

    static final Set<String> ORDENAVEIS = Set.of("id", "nome", "cargaHoraria", "categoria", "ativo");

    private final CursoRepository cursoRepository;
    private final MatriculaRepository matriculaRepository;

    @Transactional
    public Curso salvar(CursoRequestDTO dto) {
        return cursoRepository.save(aplicar(new Curso(), dto));
    }

    @Transactional(readOnly = true)
    public Page<Curso> listar(Pageable pageable) {
        OrdenacaoPermitida.validar(pageable, ORDENAVEIS);
        return cursoRepository.findAll(OrdenacaoPermitida.comDesempate(pageable));
    }

    @Transactional(readOnly = true)
    public Curso buscarPorId(Long id) {
        return cursoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Curso não encontrado com id: " + id));
    }

    /** Busca com lock de escrita; deve ser chamado dentro de uma transacao (SDR-016). */
    @Transactional
    public Curso buscarParaEscrita(Long id) {
        return cursoRepository.buscarParaEscrita(id)
            .orElseThrow(() -> new ResourceNotFoundException("Curso não encontrado com id: " + id));
    }

    @Transactional
    public Curso atualizar(Long id, CursoRequestDTO dto) {
        return cursoRepository.save(aplicar(buscarPorId(id), dto));
    }

    @Transactional
    public void remover(Long id) {
        Curso curso = buscarParaEscrita(id);
        if (matriculaRepository.existsByCursoId(id)) {
            throw new ConflictException("Não é possível excluir o curso " + id + ": existem matrículas vinculadas.");
        }
        cursoRepository.delete(curso);
    }

    private Curso aplicar(Curso curso, CursoRequestDTO dto) {
        curso.setNome(dto.nome());
        curso.setCargaHoraria(dto.cargaHoraria());
        curso.setCategoria(dto.categoria());
        curso.setAtivo(dto.ativo());
        return curso;
    }
}
