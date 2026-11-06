package com.api.livros.services;

import com.api.livros.dtos.CategoriaRequestDTO;
import com.api.livros.exceptions.ResourceNotFoundException;
import com.api.livros.models.Categoria;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CategoriaService {
    private final List<Categoria> db = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public Categoria salvar(CategoriaRequestDTO dto) {
        Categoria entity = new Categoria();
        entity.setId(idCounter.getAndIncrement());
        entity.setNome(dto.nome());
        db.add(entity);
        return entity;
    }

    public List<Categoria> listarTodos(int page, int size) {
        int start = Math.min(page * size, db.size());
        int end = Math.min((page + 1) * size, db.size());
        return new ArrayList<>(db.subList(start, end));
    }

    public Categoria buscarPorId(Long id) {
        return db.stream()
            .filter(e -> e.getId().equals(id))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com id: " + id));
    }

    public Categoria buscarPorNome(String nome) {
        return db.stream()
            .filter(e -> e.getNome().equalsIgnoreCase(nome.trim()))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com o nome: " + nome));
    }

    public Categoria atualizar(Long id, CategoriaRequestDTO dto) {
        Categoria entity = buscarPorId(id);
        entity.setNome(dto.nome());
        return entity;
    }

    public void remover(Long id) {
        Categoria entity = buscarPorId(id);
        db.remove(entity);
    }
}
