package com.api.matriculas.services;

import com.api.matriculas.dtos.AlunoRequestDTO;
import com.api.matriculas.exceptions.ConflictException;
import com.api.matriculas.exceptions.ResourceNotFoundException;
import com.api.matriculas.models.Aluno;
import com.api.matriculas.repositories.AlunoRepository;
import com.api.matriculas.repositories.MatriculaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlunoService {

    private final AlunoRepository alunoRepository;
    private final MatriculaRepository matriculaRepository;

    @Transactional
    public Aluno salvar(AlunoRequestDTO dto) {
        return alunoRepository.save(aplicar(new Aluno(), dto));
    }

    @Transactional(readOnly = true)
    public Page<Aluno> listar(Pageable pageable) {
        return alunoRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Aluno buscarPorId(Long id) {
        return alunoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Aluno não encontrado com id: " + id));
    }

    @Transactional
    public Aluno atualizar(Long id, AlunoRequestDTO dto) {
        return alunoRepository.save(aplicar(buscarPorId(id), dto));
    }

    @Transactional
    public void remover(Long id) {
        Aluno aluno = buscarPorId(id);
        if (matriculaRepository.existsByAlunoId(id)) {
            throw new ConflictException("Não é possível excluir o aluno " + id + ": existem matrículas vinculadas.");
        }
        alunoRepository.delete(aluno);
    }

    private Aluno aplicar(Aluno aluno, AlunoRequestDTO dto) {
        aluno.setNome(dto.nome());
        aluno.setEmail(dto.email());
        aluno.setIdade(dto.idade());
        aluno.setTelefone(dto.telefone());
        return aluno;
    }
}
