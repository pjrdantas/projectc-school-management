package br.com.escola.responsiblesservice.infra.observability;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import br.com.escola.responsiblesservice.application.state.LeituraModeloSyncSummary;
import br.com.escola.responsiblesservice.application.state.LeituraModeloSyncState;
import br.com.escola.responsiblesservice.infra.config.LeituraModeloProperties;

@Component("responsiblesLocalPersistence")
public class LeituraModeloHealthIndicator implements HealthIndicator {

    private final LeituraModeloProperties properties;
    private final LeituraModeloSyncState syncState;

    public LeituraModeloHealthIndicator(
            LeituraModeloProperties properties,
            LeituraModeloSyncState syncState) {
        this.properties = properties;
        this.syncState = syncState;
    }

    @Override
    public Health health() {
        LeituraModeloSyncSummary lastSummary = syncState.lastSummary();
        boolean catalogRouteReady = syncState.isCatalogRouteReady();
        boolean linkRouteReady = syncState.isLinkRouteReady();
        Health.Builder builder = catalogRouteReady || linkRouteReady ? Health.up() : Health.unknown();

        builder.withDetail("enabled", properties.enabled())
                .withDetail("migrationEnabled", properties.migrationEnabled())
                .withDetail("localReadRoutingEnabled", properties.localReadRoutingEnabled())
                .withDetail("backfillEnabled", properties.backfillEnabled())
                .withDetail("reconciliationEnabled", properties.reconciliationEnabled())
                .withDetail("fallbackEnabled", properties.fallbackEnabled())
                .withDetail("catalogRouteReady", catalogRouteReady)
                .withDetail("linkRouteReady", linkRouteReady);

        if (lastSummary != null) {
            builder.withDetail("lastStatus", lastSummary.status())
                    .withDetail("lastReason", lastSummary.reason())
                    .withDetail("plannedTables", lastSummary.plannedTables())
                    .withDetail("successfulTables", lastSummary.successfulTables())
                    .withDetail("backfilledRecords", lastSummary.backfilledRecords())
                    .withDetail("divergentRecords", lastSummary.divergentRecords())
                    .withDetail("sourceRows", lastSummary.sourceRows())
                    .withDetail("targetRows", lastSummary.targetRows());
        }
        return builder.build();
    }
}

