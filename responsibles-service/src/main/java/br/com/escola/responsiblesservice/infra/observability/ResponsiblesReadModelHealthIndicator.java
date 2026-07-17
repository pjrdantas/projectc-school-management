package br.com.escola.responsiblesservice.infra.observability;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import br.com.escola.responsiblesservice.application.state.ResponsiblesReadModelSyncSummary;
import br.com.escola.responsiblesservice.application.state.ResponsiblesReadModelSyncState;
import br.com.escola.responsiblesservice.infra.config.ResponsiblesReadModelProperties;

@Component("responsiblesLocalPersistence")
public class ResponsiblesReadModelHealthIndicator implements HealthIndicator {

    private final ResponsiblesReadModelProperties properties;
    private final ResponsiblesReadModelSyncState syncState;

    public ResponsiblesReadModelHealthIndicator(
            ResponsiblesReadModelProperties properties,
            ResponsiblesReadModelSyncState syncState) {
        this.properties = properties;
        this.syncState = syncState;
    }

    @Override
    public Health health() {
        ResponsiblesReadModelSyncSummary lastSummary = syncState.lastSummary();
        Health.Builder builder = syncState.isLinkRouteReady() ? Health.up() : Health.unknown();

        builder.withDetail("enabled", properties.enabled())
                .withDetail("migrationEnabled", properties.migrationEnabled())
                .withDetail("localReadRoutingEnabled", properties.localReadRoutingEnabled())
                .withDetail("backfillEnabled", properties.backfillEnabled())
                .withDetail("reconciliationEnabled", properties.reconciliationEnabled())
                .withDetail("fallbackEnabled", properties.fallbackEnabled())
                .withDetail("linkRouteReady", syncState.isLinkRouteReady());

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
