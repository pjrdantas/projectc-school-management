package br.com.escola.bff.infra.observability;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import br.com.escola.bff.infra.config.CatalogServiceClientProperties;
import br.com.escola.bff.infra.config.CatalogWriteCutoverProperties;
import br.com.escola.bff.infra.cutover.CatalogMigrationReportGate;

@Component("catalogWriteCutover")
public class CatalogWriteCutoverHealthIndicator implements HealthIndicator {

    private final CatalogWriteCutoverProperties properties;
    private final CatalogServiceClientProperties catalogServiceProperties;
    private final CatalogMigrationReportGate reportGate;

    public CatalogWriteCutoverHealthIndicator(
            CatalogWriteCutoverProperties properties,
            CatalogServiceClientProperties catalogServiceProperties,
            CatalogMigrationReportGate reportGate) {
        this.properties = properties;
        this.catalogServiceProperties = catalogServiceProperties;
        this.reportGate = reportGate;
    }

    @Override
    public Health health() {
        CatalogMigrationReportGate.GateStatus gateStatus = reportGate.status();
        boolean cutoverEnabled = properties.enabled();
        boolean internalTokenConfigured = catalogServiceProperties.internalToken() != null
                && !catalogServiceProperties.internalToken().isBlank();

        Health.Builder builder = !cutoverEnabled || gateStatus.allowed()
                ? Health.up()
                : Health.down();

        return builder
                .withDetail("cutoverEnabled", cutoverEnabled)
                .withDetail("routePeriodosLetivosEnabled", properties.routes().periodosLetivos())
                .withDetail("routeDisciplinasEnabled", properties.routes().disciplinas())
                .withDetail("routeSeriesEnabled", properties.routes().series())
                .withDetail("gateAllowed", gateStatus.allowed())
                .withDetail("gateReason", gateStatus.reason())
                .withDetail("internalTokenConfigured", internalTokenConfigured)
                .build();
    }
}
