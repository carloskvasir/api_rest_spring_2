#!/bin/bash
if [ -z "$1" ]; then
  echo "❌ Uso: ./bin/scaffold.sh <NomeDaEntidade> (ex: Autor)"
  exit 1
fi

ENTITY=$1
# Transforma a primeira letra em minúscula para rotas (ex: Autor -> autor)
LOWER_ENTITY=$(echo "$ENTITY" | tr '[:upper:]' '[:lower:]')
PACKAGE="com.api.livros"
BASE_DIR="src/main/java/com/api/livros"

echo "🚀 Gerando Boilerplate Profissional para: $ENTITY..."

# 1. Model
cat << EOF2 > "$BASE_DIR/models/$ENTITY.java"
package $PACKAGE.models;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class $ENTITY {
    private Long id;
    // TODO: Adicionar os campos da entidade $ENTITY
}
EOF2

# 2. DTOs
cat << EOF2 > "$BASE_DIR/dtos/${ENTITY}RequestDTO.java"
package $PACKAGE.dtos;

import jakarta.validation.constraints.NotBlank;

public record ${ENTITY}RequestDTO(
    // TODO: Adicionar campos e anotações de validação (ex: @NotBlank)
) {}
EOF2

cat << EOF2 > "$BASE_DIR/dtos/${ENTITY}ResponseDTO.java"
package $PACKAGE.dtos;

import $PACKAGE.models.$ENTITY;

public record ${ENTITY}ResponseDTO(
    Long id
    // TODO: Adicionar campos expostos
) {
    public static ${ENTITY}ResponseDTO fromEntity($ENTITY entity) {
        return new ${ENTITY}ResponseDTO(
            entity.getId()
        );
    }
}
EOF2

# 3. Service
cat << EOF2 > "$BASE_DIR/services/${ENTITY}Service.java"
package $PACKAGE.services;

import $PACKAGE.dtos.${ENTITY}RequestDTO;
import $PACKAGE.exceptions.ResourceNotFoundException;
import $PACKAGE.models.$ENTITY;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ${ENTITY}Service {
    private final List<$ENTITY> db = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public $ENTITY salvar(${ENTITY}RequestDTO dto) {
        $ENTITY entity = new $ENTITY();
        entity.setId(idCounter.getAndIncrement());
        // TODO: Mapear dto para entidade
        db.add(entity);
        return entity;
    }

    public List<$ENTITY> listarTodos(int page, int size) {
        int start = Math.min(page * size, db.size());
        int end = Math.min((page + 1) * size, db.size());
        return new ArrayList<>(db.subList(start, end));
    }

    public $ENTITY buscarPorId(Long id) {
        return db.stream()
            .filter(e -> e.getId().equals(id))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("$ENTITY não encontrado com id: " + id));
    }

    public $ENTITY atualizar(Long id, ${ENTITY}RequestDTO dto) {
        $ENTITY entity = buscarPorId(id);
        // TODO: Atualizar campos
        return entity;
    }

    public void remover(Long id) {
        $ENTITY entity = buscarPorId(id);
        db.remove(entity);
    }
}
EOF2

# 4. Controller
cat << EOF2 > "$BASE_DIR/controllers/${ENTITY}Controller.java"
package $PACKAGE.controllers;

import $PACKAGE.dtos.${ENTITY}RequestDTO;
import $PACKAGE.dtos.${ENTITY}ResponseDTO;
import $PACKAGE.models.$ENTITY;
import $PACKAGE.services.${ENTITY}Service;
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
@RequestMapping("/${LOWER_ENTITY}s")
@RequiredArgsConstructor
public class ${ENTITY}Controller {

    private final ${ENTITY}Service service;

    @PostMapping
    public ResponseEntity<${ENTITY}ResponseDTO> cadastrar(@Valid @RequestBody ${ENTITY}RequestDTO dto) {
        $ENTITY criado = service.salvar(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.getId()).toUri();
        return ResponseEntity.created(location).body(${ENTITY}ResponseDTO.fromEntity(criado));
    }

    @GetMapping
    public ResponseEntity<List<${ENTITY}ResponseDTO>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<${ENTITY}ResponseDTO> lista = service.listarTodos(page, size).stream()
                .map(${ENTITY}ResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<${ENTITY}ResponseDTO> buscarPorId(@PathVariable Long id) {
        $ENTITY entity = service.buscarPorId(id);
        return ResponseEntity.ok(${ENTITY}ResponseDTO.fromEntity(entity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<${ENTITY}ResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody ${ENTITY}RequestDTO dto) {
        $ENTITY atualizado = service.atualizar(id, dto);
        return ResponseEntity.ok(${ENTITY}ResponseDTO.fromEntity(atualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
EOF2

echo "✅ Gerador concluído! Os 5 arquivos base (Model, RequestDTO, ResponseDTO, Service e Controller) para a entidade '$ENTITY' foram injetados perfeitamente."
