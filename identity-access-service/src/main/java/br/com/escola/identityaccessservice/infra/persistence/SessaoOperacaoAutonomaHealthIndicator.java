package br.com.escola.identityaccessservice.infra.persistence;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("identityOperation")
public class SessaoOperacaoAutonomaHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        return Health.up()
                .withDetail("operationalMode", "local_only")
                .withDetail("sessionCleanupOwner", "identity-access-service")
                .build();
    }
}
