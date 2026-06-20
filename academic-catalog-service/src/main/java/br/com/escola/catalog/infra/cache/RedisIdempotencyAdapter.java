package br.com.escola.catalog.infra.cache;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import br.com.escola.catalog.application.port.out.IdempotencyPort;

@Component
public class RedisIdempotencyAdapter implements IdempotencyPort {

    private final StringRedisTemplate redisTemplate;

    public RedisIdempotencyAdapter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean registrarSeAusente(String key, Duration ttl) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue()
                .setIfAbsent("academic-catalog:idempotency:" + key, "1", ttl));
    }
}

