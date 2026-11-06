package com.api.matriculas.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AlunoRequestDTO(
    @NotBlank(message = "O nome do aluno é obrigatório")
    String nome,

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "O e-mail informado é inválido")
    String email,

    @NotNull(message = "A idade é obrigatória")
    @Min(value = 16, message = "A idade mínima é 16 anos")
    Integer idade,

    String telefone
) {}
