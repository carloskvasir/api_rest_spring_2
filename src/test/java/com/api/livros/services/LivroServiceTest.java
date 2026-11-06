package com.api.livros.services;

import com.api.livros.dtos.CategoriaRequestDTO;
import com.api.livros.dtos.LivroRequestDTO;
import com.api.livros.exceptions.ResourceNotFoundException;
import com.api.livros.models.Categoria;
import com.api.livros.models.Livro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class LivroServiceTest {

    private LivroService livroService;
    private CategoriaService categoriaService;
    private Categoria categoriaTeste;
    private Categoria categoriaTeste2;

    @BeforeEach
    void setUp() {
        categoriaService = new CategoriaService();
        livroService = new LivroService(categoriaService);
        
        // Criar uma categoria para ser usada nos testes de livros
        categoriaTeste = categoriaService.salvar(new CategoriaRequestDTO("Ficção Científica"));
        categoriaTeste2 = categoriaService.salvar(new CategoriaRequestDTO("Fantasia"));
    }

    @Test
    void deveSalvarLivroComSucesso() {
        LivroRequestDTO dto = new LivroRequestDTO("O Senhor dos Anéis", "J.R.R. Tolkien", 1200, 150.0, categoriaTeste.getId());
        Livro salvo = livroService.salvar(dto);

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getTitulo()).isEqualTo("O Senhor dos Anéis");
        assertThat(salvo.getCategoria().getNome()).isEqualTo("Ficção Científica");
        assertThat(livroService.listarTodos(0, 10, null)).hasSize(1);
    }

    @Test
    void deveFiltrarLivrosPorNomeDaCategoriaIgnorandoCaixa() {
        livroService.salvar(new LivroRequestDTO("O Senhor dos Anéis", "J.R.R. Tolkien", 1200, 150.0, categoriaTeste2.getId()));
        livroService.salvar(new LivroRequestDTO("Duna", "Frank Herbert", 800, 95.50, categoriaTeste.getId()));

        assertThat(livroService.listarTodos(0, 10, "fantasia")).hasSize(1);
        assertThat(livroService.listarTodos(0, 10, "FICÇÃO CIENTÍFICA")).hasSize(1);
    }

    @Test
    void deveLancarExcecaoAoFiltrarPorCategoriaInexistente() {
        assertThatThrownBy(() -> livroService.listarTodos(0, 10, "Aventura"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Categoria não encontrada com o nome: Aventura");
    }

    @Test
    void deveBuscarLivroPorIdExistente() {
        Livro salvo = livroService.salvar(new LivroRequestDTO("1984", "George Orwell", 328, 45.0, categoriaTeste.getId()));

        Livro encontrado = livroService.buscarPorId(salvo.getId());
        assertThat(encontrado).isNotNull();
        assertThat(encontrado.getTitulo()).isEqualTo("1984");
    }

    @Test
    void deveLancarExcecaoAoBuscarIdInexistente() {
        assertThatThrownBy(() -> livroService.buscarPorId(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Livro não encontrado com id: 999");
    }

    @Test
    void deveAtualizarParcialmenteUmLivro() {
        Livro salvo = livroService.salvar(new LivroRequestDTO("Clean Code", "Robert Martin", 400, 100.0, categoriaTeste.getId()));

        Map<String, Object> campos = Map.of("preco", 120.0); // Modifica apenas o preco
        Livro atualizado = livroService.atualizarParcial(salvo.getId(), campos);

        assertThat(atualizado.getPreco()).isEqualTo(120.0);
        assertThat(atualizado.getTitulo()).isEqualTo("Clean Code"); // Permanece inalterado
    }

    @Test
    void deveRemoverLivro() {
        Livro salvo = livroService.salvar(new LivroRequestDTO("Duna", "Frank Herbert", 600, 90.0, categoriaTeste.getId()));

        livroService.remover(salvo.getId());
        assertThat(livroService.listarTodos(0, 10, null)).isEmpty();
    }
}
