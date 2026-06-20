package br.com.escola.catalog.domain.model;

import br.com.escola.catalog.domain.exception.CatalogDomainException;

final class CatalogAssertions {

    private CatalogAssertions() {
    }

    static <T> T notNull(T value, String field) {
        if (value == null) {
            throw new CatalogDomainException(field + " nao pode ser nulo");
        }
        return value;
    }

    static String notBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new CatalogDomainException(field + " nao pode ser vazio");
        }
        return value.trim();
    }

    static int positive(int value, String field) {
        if (value <= 0) {
            throw new CatalogDomainException(field + " deve ser positivo");
        }
        return value;
    }
}

