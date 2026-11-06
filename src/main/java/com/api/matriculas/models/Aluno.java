package com.api.matriculas.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SoftDelete;

import java.util.ArrayList;
import java.util.List;

@Entity
@SoftDelete(columnName = "deleted")
@Getter
@Setter
@NoArgsConstructor
public class Aluno {

    /** Exige ao menos um ponto no dominio (o @Email padrao aceita "a@b"). */
    public static final String EMAIL_REGEX = "^[^@\\s]+@[^@\\s.]+(\\.[^@\\s.]+)+$";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do aluno é obrigatório")
    @Column(nullable = false)
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(regexp = EMAIL_REGEX, message = "O e-mail informado é inválido")
    @Column(nullable = false)
    private String email;

    @NotNull(message = "A idade é obrigatória")
    @Min(value = 16, message = "A idade mínima é 16 anos")
    @Column(nullable = false)
    private Integer idade;

    private String telefone;

    @OneToMany(mappedBy = "aluno")
    @Setter(AccessLevel.NONE)
    private List<Matricula> matriculas = new ArrayList<>();
}
