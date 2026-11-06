package com.api.matriculas;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/** O perfil prod (usado no deploy, SDR-018) desliga o console do H2 e o log de SQL. */
@SpringBootTest
@ActiveProfiles("prod")
class PerfilProdTest {

    @Autowired private Environment env;

    @Test
    void perfilProdDeveDesligarConsoleH2() {
        assertThat(env.getProperty("spring.h2.console.enabled")).isEqualTo("false");
        assertThat(env.getProperty("spring.jpa.show-sql")).isEqualTo("false");
    }
}
