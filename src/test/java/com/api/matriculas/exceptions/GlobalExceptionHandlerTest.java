package com.api.matriculas.exceptions;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.data.util.TypeInformation;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.StubController.class)
@Import({GlobalExceptionHandler.class, GlobalExceptionHandlerTest.StubController.class})
class GlobalExceptionHandlerTest {

    enum Cor { AZUL }

    record Corpo(@NotBlank(message = "nome obrigatório") String nome) {}

    @RestController
    @RequestMapping("/stub")
    static class StubController {
        @GetMapping("/negocio")
        void negocio() { throw new BusinessRuleException("regra violada"); }

        @GetMapping("/conflito")
        void conflito() { throw new ConflictException("conflito de estado"); }

        @GetMapping("/inexistente")
        void inexistente() { throw new ResourceNotFoundException("nada aqui"); }

        @PostMapping("/validar")
        void validar(@Valid @RequestBody Corpo corpo) { }

        @GetMapping("/enum")
        void enumParam(@RequestParam Cor cor) { }

        @GetMapping("/sort")
        void sort() {
            throw new PropertyReferenceException("xyz", TypeInformation.of(String.class), List.of());
        }
    }

    @Autowired private MockMvc mockMvc;

    @Test
    void businessRuleDeveRetornar422() throws Exception {
        mockMvc.perform(get("/stub/negocio"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title", is("Unprocessable Entity")))
                .andExpect(jsonPath("$.detail", is("regra violada")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void conflictDeveRetornar409() throws Exception {
        mockMvc.perform(get("/stub/conflito"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title", is("Conflict")))
                .andExpect(jsonPath("$.detail", is("conflito de estado")));
    }

    @Test
    void naoEncontradoDeveRetornar404() throws Exception {
        mockMvc.perform(get("/stub/inexistente"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title", is("Resource Not Found")));
    }

    @Test
    void validacaoDeveRetornar400ComInvalidParams() throws Exception {
        mockMvc.perform(post("/stub/validar").contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.invalid_params.nome", is("nome obrigatório")));
    }

    @Test
    void jsonMalformadoDeveRetornar400ProblemDetail() throws Exception {
        mockMvc.perform(post("/stub/validar").contentType(MediaType.APPLICATION_JSON).content("{nome:"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Bad Request")))
                .andExpect(jsonPath("$.detail", containsString("malformado")));
    }

    @Test
    void corpoComTipoErradoDeveRetornar400() throws Exception {
        mockMvc.perform(post("/stub/validar").contentType(MediaType.APPLICATION_JSON).content("[1,2]"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void tipoDeParametroInvalidoDeveRetornar400() throws Exception {
        mockMvc.perform(get("/stub/enum").param("cor", "XPTO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Bad Request")));
    }

    @Test
    void sortInvalidoDeveRetornar400() throws Exception {
        mockMvc.perform(get("/stub/sort"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Bad Request")));
    }
}
