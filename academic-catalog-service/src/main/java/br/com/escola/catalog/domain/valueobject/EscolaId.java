package br.com.escola.catalog.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public record EscolaId(UUID value) {

    public EscolaId {
        Objects.requireNonNull(value, "value nao pode ser nulo");
    }
}

