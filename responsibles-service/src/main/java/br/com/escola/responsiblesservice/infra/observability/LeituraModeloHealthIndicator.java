package br.com.escola.responsiblesservice.infra.observability;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import br.com.escola.responsiblesservice.infra.config.LeituraModeloProperties;

@Component("responsiblesLocalPersistence")
public class LeituraModeloHealthIndicator implements HealthIndicator {

    private final LeituraModeloProperties properties;

    public LeituraModeloHealthIndicator(LeituraModeloProperties properties) {
        this.properties = properties;
    }

    @Override
    public Health health() {
        boolean autonomous = properties.enabled()
                && properties.localReadRoutingEnabled()
                && !properties.fallbackEnabled()
                && !properties.backfillEnabled()
                && !properties.reconciliationEnabled();
        Health.Builder builder = autonomous ? Health.up() : Health.outOfService();
        return builder
                .withDetail("operationalMode", autonomous ? "local_only" : "invalid_configuration")
                .withDetail("localReadRoutingEnabled", properties.localReadRoutingEnabled())
                .withDetail("fallbackEnabled", properties.fallbackEnabled())
                .withDetail("backfillEnabled", properties.backfillEnabled())
                .withDetail("reconciliationEnabled", properties.reconciliationEnabled())
                .build();
    }
}
