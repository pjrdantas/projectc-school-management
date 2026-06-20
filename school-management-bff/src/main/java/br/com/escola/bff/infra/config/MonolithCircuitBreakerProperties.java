package br.com.escola.bff.infra.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("resilience.monolith")
public record MonolithCircuitBreakerProperties(
        float failureRateThreshold,
        int minimumNumberOfCalls,
        int slidingWindowSize,
        Duration openStateDuration) {
}

