package com.api.matriculas.services;

import com.api.matriculas.exceptions.OrdenacaoInvalidaException;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrdenacaoPermitidaTest {

    private static final Set<String> PERMITIDAS = Set.of("id", "nome");

    private Pageable ordenadoPor(String... propriedades) {
        return PageRequest.of(0, 10, Sort.by(propriedades));
    }

    @Test
    void semOrdenacaoNaoDeveFalhar() {
        assertThatCode(() -> OrdenacaoPermitida.validar(Pageable.unpaged(), PERMITIDAS)).doesNotThrowAnyException();
        assertThatCode(() -> OrdenacaoPermitida.validar(PageRequest.of(0, 10), PERMITIDAS)).doesNotThrowAnyException();
    }

    @Test
    void propriedadesPermitidasNaoDevemFalhar() {
        assertThatCode(() -> OrdenacaoPermitida.validar(ordenadoPor("id", "nome"), PERMITIDAS)).doesNotThrowAnyException();
    }

    @Test
    void propriedadeForaDaListaDeveLancarExcecaoListandoAsPermitidas() {
        assertThatThrownBy(() -> OrdenacaoPermitida.validar(ordenadoPor("matriculas.status"), PERMITIDAS))
                .isInstanceOf(OrdenacaoInvalidaException.class)
                .hasMessageContaining("'matriculas.status'")
                .hasMessageContaining("[id, nome]");
    }

    @Test
    void umaPropriedadeInvalidaEntreVariasDeveFalhar() {
        assertThatThrownBy(() -> OrdenacaoPermitida.validar(ordenadoPor("nome", "deleted"), PERMITIDAS))
                .isInstanceOf(OrdenacaoInvalidaException.class)
                .hasMessageContaining("'deleted'");
    }

    @Test
    void diferencaDeMaiusculasEMinusculasDeveFalhar() {
        assertThatThrownBy(() -> OrdenacaoPermitida.validar(ordenadoPor("Nome"), PERMITIDAS))
                .isInstanceOf(OrdenacaoInvalidaException.class);
    }

    @Test
    void comDesempateDeveAcrescentarIdQuandoNaoOrdenadoPorId() {
        Pageable resultado = OrdenacaoPermitida.comDesempate(PageRequest.of(1, 5, Sort.by(Sort.Direction.DESC, "categoria")));

        assertThat(resultado.getSort().toList()).extracting(Sort.Order::getProperty).containsExactly("categoria", "id");
        assertThat(resultado.getSort().getOrderFor("categoria").getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(resultado.getSort().getOrderFor("id").getDirection()).isEqualTo(Sort.Direction.ASC);
        assertThat(resultado.getPageNumber()).isEqualTo(1);
        assertThat(resultado.getPageSize()).isEqualTo(5);
    }

    @Test
    void comDesempateDeveManterAOrdenacaoQuandoJaOrdenaPorId() {
        Pageable original = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"));

        assertThat(OrdenacaoPermitida.comDesempate(original).getSort()).isEqualTo(original.getSort());
    }

    @Test
    void comDesempateSemOrdenacaoDeveOrdenarPorId() {
        Pageable resultado = OrdenacaoPermitida.comDesempate(PageRequest.of(0, 10));

        assertThat(resultado.getSort().toList()).extracting(Sort.Order::getProperty).containsExactly("id");
    }

    @Test
    void comDesempateNaoDeveAlterarPaginacaoDesativada() {
        assertThat(OrdenacaoPermitida.comDesempate(Pageable.unpaged()).isPaged()).isFalse();
    }
}
