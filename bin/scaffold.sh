#!/bin/bash
if [ -z "$1" ]; then
  echo "❌ Uso: ./bin/scaffold.sh <NomeDaEntidade> (ex: Professor)"
  exit 1
fi

ENTITY=$1
# Rota em minusculas com plural simples (ex: Professor -> /professors; ajuste manualmente se necessario)
LOWER_ENTITY=$(echo "$ENTITY" | tr '[:upper:]' '[:lower:]')
PACKAGE="com.api.matriculas"
BASE_DIR="src/main/java/com/api/matriculas"

# Nunca sobrescreve codigo existente: se qualquer arquivo alvo ja existir, aborta antes de gerar algo
EXISTENTES=""
for ALVO in "models/$ENTITY.java" "repositories/${ENTITY}Repository.java" "dtos/${ENTITY}RequestDTO.java" \
            "dtos/${ENTITY}ResponseDTO.java" "services/${ENTITY}Service.java" "controllers/${ENTITY}Controller.java"; do
  [ -e "$BASE_DIR/$ALVO" ] && EXISTENTES="$EXISTENTES\n  - $BASE_DIR/$ALVO"
done
if [ -n "$EXISTENTES" ]; then
  echo -e "❌ Abortado: arquivos ja existentes (nada foi alterado):$EXISTENTES"
  exit 1
fi

echo "🚀 Gerando Boilerplate Profissional para: $ENTITY..."

# 1. Model (entidade JPA)
cat << EOF2 > "$BASE_DIR/models/$ENTITY.java"
package $PACKAGE.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class $ENTITY {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // TODO: Adicionar os campos da entidade $ENTITY (e @SoftDelete se a remocao for logica, ver SDR-011)
}
EOF2

# 2. Repository
cat << EOF2 > "$BASE_DIR/repositories/${ENTITY}Repository.java"
package $PACKAGE.repositories;

import $PACKAGE.models.$ENTITY;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ${ENTITY}Repository extends JpaRepository<$ENTITY, Long> {
}
EOF2

# 3. DTOs
cat << EOF2 > "$BASE_DIR/dtos/${ENTITY}RequestDTO.java"
package $PACKAGE.dtos;

public record ${ENTITY}RequestDTO(
    // TODO: Adicionar campos e anotacoes de validacao (ex: @NotBlank)
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

# 4. Service
cat << EOF2 > "$BASE_DIR/services/${ENTITY}Service.java"
package $PACKAGE.services;

import $PACKAGE.dtos.${ENTITY}RequestDTO;
import $PACKAGE.exceptions.ResourceNotFoundException;
import $PACKAGE.models.$ENTITY;
import $PACKAGE.repositories.${ENTITY}Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ${ENTITY}Service {

    private final ${ENTITY}Repository repository;

    @Transactional
    public $ENTITY salvar(${ENTITY}RequestDTO dto) {
        $ENTITY entity = new $ENTITY();
        // TODO: Mapear dto para entidade
        return repository.save(entity);
    }

    @Transactional(readOnly = true)
    public Page<$ENTITY> listar(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public $ENTITY buscarPorId(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("$ENTITY não encontrado com id: " + id));
    }

    @Transactional
    public $ENTITY atualizar(Long id, ${ENTITY}RequestDTO dto) {
        $ENTITY entity = buscarPorId(id);
        // TODO: Atualizar campos
        return repository.save(entity);
    }

    @Transactional
    public void remover(Long id) {
        repository.delete(buscarPorId(id));
    }
}
EOF2

# 5. Controller
cat << EOF2 > "$BASE_DIR/controllers/${ENTITY}Controller.java"
package $PACKAGE.controllers;

import $PACKAGE.dtos.${ENTITY}RequestDTO;
import $PACKAGE.dtos.${ENTITY}ResponseDTO;
import $PACKAGE.models.$ENTITY;
import $PACKAGE.services.${ENTITY}Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

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
    public ResponseEntity<Page<${ENTITY}ResponseDTO>> listar(Pageable pageable) {
        return ResponseEntity.ok(service.listar(pageable).map(${ENTITY}ResponseDTO::fromEntity));
    }

    @GetMapping("/{id}")
    public ResponseEntity<${ENTITY}ResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(${ENTITY}ResponseDTO.fromEntity(service.buscarPorId(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<${ENTITY}ResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody ${ENTITY}RequestDTO dto) {
        return ResponseEntity.ok(${ENTITY}ResponseDTO.fromEntity(service.atualizar(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
EOF2

echo "✅ Gerador concluído! Os 6 arquivos base (Model, Repository, RequestDTO, ResponseDTO, Service e Controller) para a entidade '$ENTITY' foram gerados."
