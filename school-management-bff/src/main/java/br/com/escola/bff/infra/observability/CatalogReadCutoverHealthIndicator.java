package br.com.escola.bff.infra.observability;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import br.com.escola.bff.infra.config.CatalogReadCutoverProperties;
import br.com.escola.bff.infra.cutover.CatalogMigrationReportGate;

@Component("catalogReadCutover")
public class CatalogReadCutoverHealthIndicator implements HealthIndicator {

    private final CatalogReadCutoverProperties properties;
    private final CatalogMigrationReportGate reportGate;

    public CatalogReadCutoverHealthIndicator(
            CatalogReadCutoverProperties properties,
            CatalogMigrationReportGate reportGate) {
        this.properties = properties;
        this.reportGate = reportGate;
    }

    @Override
    public Health health() {
        CatalogMigrationReportGate.GateStatus gateStatus = reportGate.status();
        Health.Builder builder = statusBuilder(gateStatus);
        builder.withDetail("cutoverEnabled", properties.enabled());
        builder.withDetail("fallbackToMonolithOnError", properties.fallbackToMonolithOnError());
        builder.withDetail("gateAllowed", gateStatus.allowed());
        builder.withDetail("gateReason", gateStatus.reason());
        builder.withDetail("reportFilePresent", gateStatus.reportFilePresent());
        builder.withDetail("reportParsed", gateStatus.reportParsed());
        if (gateStatus.reportPath() != null) {
            builder.withDetail("reportPath", gateStatus.reportPath().toString());
        }
        if (gateStatus.lastModified() != null) {
            builder.withDetail("reportLastModified", gateStatus.lastModified().toString());
        }
        return builder.build();
    }

    private Health.Builder statusBuilder(CatalogMigrationReportGate.GateStatus gateStatus) {
        if (!properties.enabled()) {
            return Health.up();
        }
        return gateStatus.allowed() ? Health.up() : Health.down();
    }
}
