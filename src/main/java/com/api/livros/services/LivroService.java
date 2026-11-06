package com.api.livros.services;

import com.api.livros.dtos.LivroRequestDTO;
import com.api.livros.exceptions.ResourceNotFoundException;
import com.api.livros.models.Categoria;
import com.api.livros.models.Livro;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LivroService {
    private final List<Livro> livros = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);
    
    private final CategoriaService categoriaService;

    public Livro salvar(LivroRequestDTO dto) {
        Categoria categoria = categoriaService.buscarPorId(dto.categoriaId());
        
        Livro livro = new Livro();
        livro.setId(idCounter.getAndIncrement());
        livro.setTitulo(dto.titulo());
        livro.setAutor(dto.autor());
        livro.setQuantidadePaginas(dto.quantidadePaginas());
        livro.setPreco(dto.preco());
        livro.setCategoria(categoria);
        
        livros.add(livro);
        return livro;
    }

    public List<Livro> listarTodos(int page, int size, String categoriaNome) {
        List<Livro> stream = livros;
        
        if (categoriaNome != null && !categoriaNome.isBlank()) {
            // Garante que a categoria buscada existe, senao joga ResourceNotFoundException 404
            Categoria categoria = categoriaService.buscarPorNome(categoriaNome);
            
            stream = livros.stream()
                .filter(l -> l.getCategoria() != null && 
                             l.getCategoria().getId().equals(categoria.getId()))
                .collect(Collectors.toList());
        }
        
        int start = Math.min(page * size, stream.size());
        int end = Math.min((page + 1) * size, stream.size());
        return new ArrayList<>(stream.subList(start, end));
    }

    public Livro buscarPorId(Long id) {
        return livros.stream()
            .filter(l -> l.getId().equals(id))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Livro não encontrado com id: " + id));
    }

    public Livro atualizar(Long id, LivroRequestDTO dto) {
        Livro livro = buscarPorId(id);
        Categoria categoria = categoriaService.buscarPorId(dto.categoriaId());
        
        livro.setTitulo(dto.titulo());
        livro.setAutor(dto.autor());
        livro.setQuantidadePaginas(dto.quantidadePaginas());
        livro.setPreco(dto.preco());
        livro.setCategoria(categoria);
        return livro;
    }

    public Livro atualizarParcial(Long id, Map<String, Object> campos) {
        Livro livro = buscarPorId(id);
        campos.forEach((campo, valor) -> {
            switch (campo) {
                case "titulo" -> livro.setTitulo((String) valor);
                case "autor" -> livro.setAutor((String) valor);
                case "quantidadePaginas" -> livro.setQuantidadePaginas((Integer) valor);
                case "preco" -> {
                    if (valor instanceof Integer v) {
                        livro.setPreco(v.doubleValue());
                    } else if (valor instanceof Double v) {
                        livro.setPreco(v);
                    }
                }
                case "categoriaId" -> {
                    if (valor instanceof Integer v) {
                        Categoria cat = categoriaService.buscarPorId(v.longValue());
                        livro.setCategoria(cat);
                    } else if (valor instanceof Long v) {
                        Categoria cat = categoriaService.buscarPorId(v);
                        livro.setCategoria(cat);
                    }
                }
            }
        });
        return livro;
    }

    public void remover(Long id) {
        Livro livro = buscarPorId(id);
        livros.remove(livro);
    }
}
