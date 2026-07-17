package br.com.escola.catalog.application.service;

import java.time.Duration;

public record CacheSettings(boolean enabled, Duration ttl) {

    public CacheSettings {
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("TTL do cache deve ser positivo");
        }
    }
}

