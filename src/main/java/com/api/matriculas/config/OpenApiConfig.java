package com.api.matriculas.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        String projectDescription = """
            **API REST de Cursos, Alunos e Matrículas** desenvolvida em Java 21 e Spring Boot 3.3 (Trabalho Prático 2).
            
            Os dados são persistidos com **Spring Data JPA** em banco **H2** (em memória). A arquitetura é registrada
            em *SDRs (System Design Records)* e coberta por **Testes Unitários e de Integração**.
            
            ### 👥 Aluno
            * Carlos Kvasir Lima
            
            ### Destaques:
            * **CRUD completo** de Cursos, Alunos e Matrículas, com relacionamentos `@OneToMany` / `@ManyToOne`.
            * **Paginação** com `Pageable` (`page`, `size`, `sort`) em todas as listagens.
            * **Bean Validation** com mensagens de erro padronizadas em **RFC 7807 (Problem Details)**.
            * **Soft delete** em Cursos e Alunos; cancelamento de matrícula preserva o histórico.
            * **Regras de negócio:** curso inativo, aluno menor de 16 anos, exclusão com matrículas e matrícula ativa duplicada.
            
            ### 🌱 Seeds Automáticos
            O ambiente sobe com cursos, alunos e matrículas de exemplo para testar os endpoints `GET` imediatamente.
            """;

        return new OpenAPI()
                .info(new Info()
                        .title("API REST de Cursos e Matrículas")
                        .version("2.0.0")
                        .description(projectDescription)
                        .contact(new Contact()
                                .name("Carlos Kvasir Lima")
                                .url("https://carloskvasir.dev")
                                .email("gpg@kvasir.dev"))
                        .license(new License()
                                .name("MPL 2.0")
                                .url("https://www.mozilla.org/en-US/MPL/2.0/")));
    }
}
