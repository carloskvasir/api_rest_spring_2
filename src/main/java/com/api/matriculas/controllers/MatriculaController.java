package com.api.matriculas.controllers;

import com.api.matriculas.dtos.MatriculaRequestDTO;
import com.api.matriculas.dtos.MatriculaResponseDTO;
import com.api.matriculas.dtos.MatriculaStatusRequestDTO;
import com.api.matriculas.models.Matricula;
import com.api.matriculas.services.MatriculaService;
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
@RequestMapping("/matriculas")
@RequiredArgsConstructor
public class MatriculaController {

    private final MatriculaService matriculaService;

    @PostMapping
    public ResponseEntity<MatriculaResponseDTO> realizar(@Valid @RequestBody MatriculaRequestDTO dto) {
        Matricula criada = matriculaService.realizar(dto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criada.getId())
                .toUri();

        return ResponseEntity.created(location).body(MatriculaResponseDTO.fromEntity(criada));
    }

    @GetMapping
    public ResponseEntity<Page<MatriculaResponseDTO>> listar(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(matriculaService.listar(pageable).map(MatriculaResponseDTO::fromEntity));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatriculaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(MatriculaResponseDTO.fromEntity(matriculaService.buscarPorId(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MatriculaResponseDTO> atualizarStatus(@PathVariable Long id,
                                                                @Valid @RequestBody MatriculaStatusRequestDTO dto) {
        return ResponseEntity.ok(MatriculaResponseDTO.fromEntity(matriculaService.atualizarStatus(id, dto.status())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        matriculaService.cancelar(id);
        return ResponseEntity.noContent().build();
    }
}
