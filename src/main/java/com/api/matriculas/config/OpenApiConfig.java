package com.api.matriculas.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.method.HandlerMethod;
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
            
            ### 👥 Alunos
            * Carlos Kvasir Lima
            * David Marlon
            
            ### Destaques:
            * **CRUD completo** de Cursos, Alunos e Matrículas, com relacionamentos `@OneToMany` / `@ManyToOne`.
            * **Paginação** com `Pageable` (`page`, `size`, `sort`) em todas as listagens.
            * **Bean Validation** com mensagens de erro padronizadas em **RFC 7807 (Problem Details)**.
            * **Soft delete** em Cursos e Alunos; cancelamento de matrícula preserva o histórico.
            * **Regras de negócio:** curso inativo, aluno menor de 16 anos, exclusão com matrículas e matrícula ativa duplicada.
            
            ### 🌱 Seeds Automáticos
            O ambiente sobe com cursos, alunos e matrículas de exemplo para testar os endpoints `GET` imediatamente.
            """;

        Schema<?> problemDetail = new ObjectSchema()
                .description("Resposta de erro no formato RFC 7807 (Problem Details)")
                .addProperty("type", new StringSchema().example("https://api.matriculas.com/errors/not-found"))
                .addProperty("title", new StringSchema().example("Not Found"))
                .addProperty("status", new IntegerSchema().example(404))
                .addProperty("detail", new StringSchema())
                .addProperty("instance", new StringSchema())
                .addProperty("timestamp", new StringSchema().format("date-time"))
                .addProperty("invalid_params", new ObjectSchema().additionalProperties(new StringSchema())
                        .description("Campo -> mensagem (somente em erros de validação)"));

        return new OpenAPI()
                .components(new Components().addSchemas("ProblemDetail", problemDetail))
                .info(new Info()
                        .title("API REST de Cursos e Matrículas")
                        .version("2.0.0")
                        .description(projectDescription)
                        .contact(new Contact()
                                .name("Carlos Kvasir e David Marlon")
                                .url("https://carloskvasir.dev")
                                .email("gpg@kvasir.dev"))
                        .license(new License()
                                .name("MPL 2.0")
                                .url("https://www.mozilla.org/en-US/MPL/2.0/")));
    }

    /**
     * Documenta os codigos de sucesso e de erro (ProblemDetail) de cada operacao a partir das regras do projeto
     * (SDR-013/017), sem repetir @ApiResponse em todo metodo dos controllers.
     */
    @Bean
    public OperationCustomizer respostasPadronizadas() {
        return (Operation operacao, HandlerMethod metodo) -> {
            boolean matricula = metodo.getBeanType().getSimpleName().startsWith("Matricula");
            boolean criar = metodo.hasMethodAnnotation(PostMapping.class);
            boolean atualizar = metodo.hasMethodAnnotation(PutMapping.class);
            boolean remover = metodo.hasMethodAnnotation(DeleteMapping.class);
            boolean listar = metodo.hasMethodAnnotation(GetMapping.class)
                    && metodo.getMethod().getParameterCount() == 1
                    && metodo.getMethod().getParameterTypes()[0] == org.springframework.data.domain.Pageable.class;
            boolean porId = metodo.hasMethodAnnotation(GetMapping.class) && !listar;

            ApiResponses respostas = operacao.getResponses() == null ? new ApiResponses() : operacao.getResponses();
            operacao.setResponses(respostas);
            if (criar) {
                respostas.remove("200");
                respostas.addApiResponse("201", new ApiResponse().description("Criado (header Location com o novo recurso)"));
            }
            if (remover) {
                respostas.remove("200");
                respostas.addApiResponse("204", new ApiResponse()
                        .description(matricula ? "Matrícula cancelada (idempotente)" : "Removido (soft delete)"));
            }
            if (criar || atualizar || listar) {
                erro(respostas, "400", "Requisição inválida (validação, JSON malformado, ordenação não permitida)");
            }
            if (atualizar || remover || porId || (criar && matricula)) {
                erro(respostas, "404", "Recurso não encontrado");
            }
            if (matricula && (criar || atualizar)) {
                erro(respostas, "409", "Já existe matrícula ATIVA do aluno neste curso");
                erro(respostas, "422", "Curso inativo ou aluno menor de 16 anos");
            }
            if (remover && !matricula) {
                erro(respostas, "409", "Possui matrículas e não pode ser excluído");
            }
            erro(respostas, "500", "Erro interno");
            return operacao;
        };
    }

    private static void erro(ApiResponses respostas, String codigo, String descricao) {
        respostas.addApiResponse(codigo, new ApiResponse().description(descricao).content(new Content()
                .addMediaType("application/problem+json",
                        new MediaType().schema(new Schema<>().$ref("#/components/schemas/ProblemDetail")))));
    }
}
