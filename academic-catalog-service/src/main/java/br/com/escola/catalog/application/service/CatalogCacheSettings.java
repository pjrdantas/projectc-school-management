package br.com.escola.catalog.application.service;

import java.time.Duration;

public record CatalogCacheSettings(boolean enabled, Duration ttl) {

    public CatalogCacheSettings {
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("TTL do cache deve ser positivo");
        }
    }
}
