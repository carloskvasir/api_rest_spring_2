package com.api.livros.controllers;

import com.api.livros.dtos.LivroRequestDTO;
import com.api.livros.dtos.LivroResponseDTO;
import com.api.livros.models.Livro;
import com.api.livros.services.LivroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/livros")
@RequiredArgsConstructor
public class LivroController {

    private final LivroService livroService;

    @PostMapping
    public ResponseEntity<LivroResponseDTO> cadastrar(@Valid @RequestBody LivroRequestDTO dto) {
        Livro livroCriado = livroService.salvar(dto);
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(livroCriado.getId())
                .toUri();

        return ResponseEntity.created(location).body(LivroResponseDTO.fromEntity(livroCriado));
    }

    @GetMapping
    public ResponseEntity<List<LivroResponseDTO>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String categoria) {
        
        List<LivroResponseDTO> lista = livroService.listarTodos(page, size, categoria).stream()
                .map(LivroResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LivroResponseDTO> buscarPorId(@PathVariable Long id) {
        Livro livro = livroService.buscarPorId(id);
        return ResponseEntity.ok(LivroResponseDTO.fromEntity(livro));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LivroResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody LivroRequestDTO dto) {
        Livro livroAtualizado = livroService.atualizar(id, dto);
        return ResponseEntity.ok(LivroResponseDTO.fromEntity(livroAtualizado));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<LivroResponseDTO> atualizarParcial(@PathVariable Long id, @RequestBody Map<String, Object> campos) {
        Livro livroAtualizado = livroService.atualizarParcial(id, campos);
        return ResponseEntity.ok(LivroResponseDTO.fromEntity(livroAtualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        livroService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
