package br.com.escola.catalog.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import br.com.escola.catalog.application.cache.CatalogReadSnapshot;
import br.com.escola.catalog.application.dto.DisciplinaResponse;
import br.com.escola.catalog.infra.cache.RedisCatalogReadCacheAdapter;
import br.com.escola.catalog.domain.valueobject.EscolaId;

@Testcontainers
@SpringBootTest(properties = {
        "catalog.cache.enabled=true",
        "catalog.cache.kafka.enabled=false",
        "catalog.cache.environment=integration",
        "catalog.cache.ttl=PT1M",
        "management.health.redis.enabled=false"
})
class CatalogRedisCacheIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>("redis:8-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
        registry.add("spring.data.redis.timeout", () -> "PT1S");
    }

    @Autowired
    private RedisCatalogReadCacheAdapter cache;

    @Autowired
    private StringRedisTemplate redis;

    @Test
    void deveIsolarPorEscolaAplicarTtlEOperarFailOpen() {
        EscolaId escolaA = new EscolaId(UUID.randomUUID());
        EscolaId escolaB = new EscolaId(UUID.randomUUID());
        DisciplinaResponse disciplina = new DisciplinaResponse(
                UUID.randomUUID(), "Matematica", 80, true, escolaA.value(), LocalDateTime.now());
        CatalogReadSnapshot snapshot = new CatalogReadSnapshot(
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(disciplina), List.of());

        cache.armazenar(escolaA, snapshot, Duration.ofMinutes(1));

        assertThat(cache.buscar(escolaA)).contains(snapshot);
        assertThat(cache.buscar(escolaB)).isEmpty();
        String key = "integration:academic-catalog:" + escolaA.value() + ":catalog-read:v1";
        assertThat(redis.getExpire(key)).isBetween(1L, 60L);

        REDIS.stop();
        assertThatCode(() -> assertThat(cache.buscar(escolaA)).isEmpty()).doesNotThrowAnyException();
    }
}
