package com.api.matriculas.controllers;

import com.api.matriculas.dtos.CursoRequestDTO;
import com.api.matriculas.dtos.CursoResponseDTO;
import com.api.matriculas.models.Curso;
import com.api.matriculas.services.CursoService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/cursos")
@RequiredArgsConstructor
public class CursoController {

    private final CursoService cursoService;

    @PostMapping
    public ResponseEntity<CursoResponseDTO> cadastrar(@Valid @RequestBody CursoRequestDTO dto) {
        Curso criado = cursoService.salvar(dto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.getId())
                .toUri();

        return ResponseEntity.created(location).body(CursoResponseDTO.fromEntity(criado));
    }

    @GetMapping
    public ResponseEntity<Page<CursoResponseDTO>> listar(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(cursoService.listar(pageable).map(CursoResponseDTO::fromEntity));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CursoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(CursoResponseDTO.fromEntity(cursoService.buscarPorId(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CursoResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody CursoRequestDTO dto) {
        return ResponseEntity.ok(CursoResponseDTO.fromEntity(cursoService.atualizar(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        cursoService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
