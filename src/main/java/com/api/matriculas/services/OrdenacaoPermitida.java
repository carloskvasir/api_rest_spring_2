package com.api.matriculas.services;

import com.api.matriculas.exceptions.OrdenacaoInvalidaException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;
import java.util.TreeSet;

/**
 * Valida as propriedades de ordenacao aceitas nas listagens (SDR-012).
 * Ordenar por colecoes (ex.: {@code matriculas.status}) gera join e repete itens entre paginas.
 */
final class OrdenacaoPermitida {

    private OrdenacaoPermitida() {
    }

    static void validar(Pageable pageable, Set<String> permitidas) {
        for (Sort.Order ordem : pageable.getSort()) {
            if (!permitidas.contains(ordem.getProperty())) {
                throw new OrdenacaoInvalidaException("Propriedade de ordenação não permitida: '"
                        + ordem.getProperty() + "'. Permitidas: " + new TreeSet<>(permitidas) + ".");
            }
        }
    }

    /**
     * Acrescenta {@code id} como criterio de desempate. Sem ele, ordenar por um campo com valores repetidos
     * deixa a ordem entre os empatados indefinida e itens podem repetir ou sumir entre paginas.
     */
    static Pageable comDesempate(Pageable pageable) {
        if (pageable.isUnpaged() || pageable.getSort().getOrderFor("id") != null) {
            return pageable;
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort().and(Sort.by("id")));
    }
}
