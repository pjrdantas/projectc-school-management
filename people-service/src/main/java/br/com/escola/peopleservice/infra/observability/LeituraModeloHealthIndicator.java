package br.com.escola.peopleservice.infra.observability;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import br.com.escola.peopleservice.application.service.LeituraModeloMigrationState;
import br.com.escola.peopleservice.application.service.OrigemLeituraPolicy;
import br.com.escola.peopleservice.infra.config.LeituraModeloProperties;

@Component("peopleReadModel")
public class LeituraModeloHealthIndicator implements HealthIndicator {

    private final LeituraModeloProperties properties;
    private final OrigemLeituraPolicy readRoutingPolicy;
    private final LeituraModeloMigrationState migrationState;

    public LeituraModeloHealthIndicator(
            LeituraModeloProperties properties,
            OrigemLeituraPolicy readRoutingPolicy,
            LeituraModeloMigrationState migrationState) {
        this.properties = properties;
        this.readRoutingPolicy = readRoutingPolicy;
        this.migrationState = migrationState;
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
                .withDetail("legacyTrafficEnabled", false)
                .withDetail("localReadRoutingEnabled", properties.localReadRoutingEnabled())
                .withDetail("fallbackEnabled", properties.fallbackEnabled())
                .withDetail("backfillEnabled", properties.backfillEnabled())
                .withDetail("reconciliationEnabled", properties.reconciliationEnabled())
                .withDetail("routes", readRoutingPolicy.avaliarTodas())
                .withDetail("schemaMigration", migrationState.currentReport())
                .build();
    }
}
