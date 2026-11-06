package com.api.matriculas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

/**
 * Paginacao (SDR-012): serializa {@code Page} em formato estavel ({@code content} + {@code page}) e
 * limita o tamanho da pagina. As propriedades {@code spring.data.web.pageable.*} nao sao lidas quando
 * {@code @EnableSpringDataWebSupport} esta ativo, por isso os limites ficam aqui.
 */
@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class PaginacaoConfig {

    static final int TAMANHO_PADRAO = 10;
    static final int TAMANHO_MAXIMO = 100;

    @Bean
    PageableHandlerMethodArgumentResolverCustomizer limitesDePaginacao() {
        return resolver -> {
            resolver.setFallbackPageable(PageRequest.of(0, TAMANHO_PADRAO));
            resolver.setMaxPageSize(TAMANHO_MAXIMO);
        };
    }
}
