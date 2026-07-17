package br.com.escola.bff.infra.observability;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import br.com.escola.bff.infra.config.IdentityTenantCutoverProperties;

@Component("identityTenantCutover")
public class IdentityTenantCutoverHealthIndicator implements HealthIndicator {

    private final IdentityTenantCutoverProperties properties;

    public IdentityTenantCutoverHealthIndicator(IdentityTenantCutoverProperties properties) {
        this.properties = properties;
    }

    @Override
    public Health health() {
        return Health.up()
                .withDetail("cutoverEnabled", properties.enabled())
                .withDetail("fallbackToLegacyOnError", properties.fallbackToLegacyOnError())
                .withDetail("authEscolasEnabled", properties.routes().authEscolas())
                .withDetail("authEscolaAtivaEnabled", properties.routes().authEscolaAtiva())
                .withDetail("authTenantAtivaEnabled", properties.routes().authTenantAtiva())
                .build();
    }
}

