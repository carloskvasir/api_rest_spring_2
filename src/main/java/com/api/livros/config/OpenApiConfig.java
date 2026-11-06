package com.api.livros.config;

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
            **API REST de Controle de Livros** desenvolvida em Java 21 e Spring Boot 3.3.
            
            O armazenamento dos dados é realizado temporariamente em memória. Este sistema foi 
            cuidadosamente arquitetado através de *SDRs (System Design Reviews)* e é focado na adoção de boas 
            práticas e Cultura de Qualidade, incluindo cobertura rígida de **Testes Unitários e de Integração**.
            
            ### 👥 Alunos
            * Carlos Kvasir Lima
            * David Marlon
            
            ### Destaques e Adoção de Padrões RESTful:
            * **RFC 7807 (Problem Details):** Tratamento padronizado de exceções e erros HTTP para clientes.
            * **Header Location:** O endpoint de `POST` retorna HTTP 201 Created acompanhado do cabeçalho seguro para a URI do novo recurso.
            * **Atualização Parcial:** Suporte ao método `PATCH` para atualizações eficientes e dinâmicas, além do clássico `PUT` integral.
            
            ### 🌱 Seeds Automáticos
            O ambiente de desenvolvimento (esta interface) sobe com a base de dados populada por 5 livros clássicos (Duna, 1984, etc.) para que você possa testar os endpoints `GET` imediatamente, sem precisar realizar cadastros manuais prévios!
            """;

        return new OpenAPI()
                .info(new Info()
                        .title("API REST de Controle de Livros")
                        .version("1.7.1")
                        .description(projectDescription)
                        .contact(new Contact()
                                .name("Carlos Kvasir e David Marlon")
                                .url("https://carloskvasir.dev")
                                .email("gpg@kvasir.dev"))
                        .license(new License()
                                .name("MPL 2.0")
                                .url("https://www.mozilla.org/en-US/MPL/2.0/")));
    }
}
