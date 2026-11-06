package com.api.livros.controllers;

import com.api.livros.dtos.LivroRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LivroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deveCadastrarLivroERetornar201ComLocationHeader() throws Exception {
        LivroRequestDTO dto = new LivroRequestDTO("O Hobbit", "J.R.R. Tolkien", 300, 60.0, 1L);

        mockMvc.perform(post("/livros")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location")) // RFC RESTful (SDR-002)
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.titulo", is("O Hobbit")))
                .andExpect(jsonPath("$.categoria.nome", notNullValue()));
    }

    @Test
    void deveRetornar400AoCadastrarComValidacaoFalhaESeguirRFC7807() throws Exception {
        // Título inválido (min 3), paginas negativo, preco negativo, categoria nula
        LivroRequestDTO dto = new LivroRequestDTO("Oi", "Autor", 0, -10.0, null);

        mockMvc.perform(post("/livros")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Bad Request"))) // ProblemDetail
                .andExpect(jsonPath("$.invalid_params.titulo", notNullValue()))
                .andExpect(jsonPath("$.invalid_params.quantidadePaginas", notNullValue()))
                .andExpect(jsonPath("$.invalid_params.preco", notNullValue()))
                .andExpect(jsonPath("$.invalid_params.categoriaId", notNullValue()));
    }

    @Test
    void deveRetornar404AoBuscarLivroInexistente() throws Exception {
        mockMvc.perform(get("/livros/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title", is("Resource Not Found")))
                .andExpect(jsonPath("$.detail", containsString("Livro não encontrado")));
    }

    @Test
    void deveRetornar404AoBuscarPorCategoriaInexistente() throws Exception {
        mockMvc.perform(get("/livros?categoria=Invalida_Nao_Existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title", is("Resource Not Found")))
                .andExpect(jsonPath("$.detail", containsString("Categoria não encontrada com o nome: Invalida_Nao_Existe")));
    }
}
