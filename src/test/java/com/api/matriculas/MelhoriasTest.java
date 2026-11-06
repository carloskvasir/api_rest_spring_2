package com.api.matriculas;

import com.api.matriculas.services.MatriculaService;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureMockMvc
class MelhoriasTest {

    @Autowired private MatriculaService matriculaService;
    @Autowired private EntityManagerFactory emf;
    @Autowired private MockMvc mockMvc;

    @Test
    void listarMatriculasNaoDeveFazerUmaConsultaPorLinha() {
        Statistics estatisticas = emf.unwrap(SessionFactory.class).getStatistics();
        estatisticas.clear();

        var pagina = matriculaService.listar(PageRequest.of(0, 10));
        pagina.getContent().forEach(m -> { m.getAluno().getNome(); m.getCurso().getNome(); });

        assertThat(pagina.getContent()).hasSizeGreaterThanOrEqualTo(4);
        // 1 select da pagina (com aluno e curso) + 1 count
        assertThat(estatisticas.getPrepareStatementCount()).isLessThanOrEqualTo(2);
    }

    @Test
    void telefoneInvalidoDeveSerRejeitado() throws Exception {
        mockMvc.perform(post("/alunos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana\",\"email\":\"ana@x.com\",\"idade\":20,\"telefone\":\"abc\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.invalid_params.telefone", is("O telefone informado é inválido")));
    }

    @Test
    void telefonesValidosOuAusentesDevemSerAceitos() throws Exception {
        for (String tel : new String[]{"\"(11) 98888-0001\"", "\"+55 11 98888-0001\"", "\"11988880001\"", "\"\"", "null"}) {
            mockMvc.perform(post("/alunos").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"nome\":\"Ana\",\"email\":\"ana@x.com\",\"idade\":20,\"telefone\":" + tel + "}"))
                    .andExpect(status().isCreated());
        }
    }
}
