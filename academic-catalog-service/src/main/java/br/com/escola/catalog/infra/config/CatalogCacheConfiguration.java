package br.com.escola.catalog.infra.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.escola.catalog.application.service.CatalogCacheSettings;

@Configuration
public class CatalogCacheConfiguration {

    @Bean
    CatalogCacheSettings catalogCacheSettings(
            @Value("${catalog.cache.enabled:false}") boolean enabled,
            @Value("${catalog.cache.ttl:PT5M}") Duration ttl) {
        return new CatalogCacheSettings(enabled, ttl);
    }
}
