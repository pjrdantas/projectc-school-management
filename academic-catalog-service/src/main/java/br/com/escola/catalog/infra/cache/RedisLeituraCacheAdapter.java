package br.com.escola.catalog.infra.cache;

import java.time.Duration;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.escola.catalog.application.cache.LeituraSnapshot;
import br.com.escola.catalog.application.port.out.LeituraCachePort;
import br.com.escola.catalog.application.service.CacheSettings;
import br.com.escola.catalog.domain.valueobject.EscolaId;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class RedisLeituraCacheAdapter implements LeituraCachePort {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisLeituraCacheAdapter.class);
    private static final String CACHE_VERSION = "v1";

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    private final CacheSettings settings;
    private final String environment;

    public RedisLeituraCacheAdapter(
            StringRedisTemplate redis,
            ObjectMapper objectMapper,
            MeterRegistry meterRegistry,
            CacheSettings settings,
            @Value("${catalog.cache.environment:local}") String environment) {
        this.redis = redis;
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;
        this.settings = settings;
        this.environment = environment;
    }

    @Override
    public Optional<LeituraSnapshot> buscar(EscolaId escolaId) {
        if (!settings.enabled()) {
            return Optional.empty();
        }
        String key = key(escolaId);
        try {
            String json = redis.opsForValue().get(key);
            if (json == null) {
                meterRegistry.counter("catalog.cache.miss").increment();
                return Optional.empty();
            }
            LeituraSnapshot snapshot = objectMapper.readValue(json, LeituraSnapshot.class);
            meterRegistry.counter("catalog.cache.hit").increment();
            return Optional.of(snapshot);
        } catch (RuntimeException | JsonProcessingException exception) {
            failOpen("leitura", key, exception);
            return Optional.empty();
        }
    }

    @Override
    public void armazenar(EscolaId escolaId, LeituraSnapshot snapshot, Duration ttl) {
        if (!settings.enabled()) {
            return;
        }
        String key = key(escolaId);
        try {
            redis.opsForValue().set(key, objectMapper.writeValueAsString(snapshot), ttl);
            meterRegistry.counter("catalog.cache.write").increment();
        } catch (RuntimeException | JsonProcessingException exception) {
            failOpen("escrita", key, exception);
        }
    }

    @Override
    public void invalidar(EscolaId escolaId) {
        if (!settings.enabled()) {
            return;
        }
        String key = key(escolaId);
        try {
            redis.delete(key);
            meterRegistry.counter("catalog.cache.invalidation").increment();
        } catch (RuntimeException exception) {
            failOpen("invalidacao", key, exception);
        }
    }

    String key(EscolaId escolaId) {
        return environment + ":academic-catalog:" + escolaId.value() + ":catalog-read:" + CACHE_VERSION;
    }

    private void failOpen(String operation, String key, Exception exception) {
        meterRegistry.counter("catalog.cache.fail_open", "operation", operation).increment();
        LOGGER.warn("Falha de cache Redis; operacao={} key={} fallback=postgresql", operation, key,
                exception);
    }
}

