package com.api.livros.config;

import com.api.livros.dtos.CategoriaRequestDTO;
import com.api.livros.dtos.LivroRequestDTO;
import com.api.livros.services.CategoriaService;
import com.api.livros.services.LivroService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CategoriaService categoriaService;
    private final LivroService livroService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🌱 Semeando dados iniciais (Seeds)...");

        // Categorias
        var catFiccao = categoriaService.salvar(new CategoriaRequestDTO("Ficção Científica"));
        var catFantasia = categoriaService.salvar(new CategoriaRequestDTO("Fantasia"));
        var catTecnologia = categoriaService.salvar(new CategoriaRequestDTO("Tecnologia"));

        // Livros
        livroService.salvar(new LivroRequestDTO(
                "O Senhor dos Anéis", "J.R.R. Tolkien", 1200, 150.0, catFantasia.getId()
        ));
        
        livroService.salvar(new LivroRequestDTO(
                "Duna", "Frank Herbert", 800, 95.50, catFiccao.getId()
        ));
        
        livroService.salvar(new LivroRequestDTO(
                "Clean Code", "Robert C. Martin", 464, 200.0, catTecnologia.getId()
        ));

        livroService.salvar(new LivroRequestDTO(
                "Neuromancer", "William Gibson", 320, 75.0, catFiccao.getId()
        ));

        livroService.salvar(new LivroRequestDTO(
                "O Nome do Vento", "Patrick Rothfuss", 662, 110.0, catFantasia.getId()
        ));

        System.out.println("✅ Seeds inseridos com sucesso!");
    }
}
