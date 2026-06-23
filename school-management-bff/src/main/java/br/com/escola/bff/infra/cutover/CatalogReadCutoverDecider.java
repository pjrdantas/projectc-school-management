package br.com.escola.bff.infra.cutover;

import org.springframework.stereotype.Component;

import br.com.escola.bff.application.port.out.CatalogReadCutoverPolicyPort;
import br.com.escola.bff.application.service.CatalogReadCutoverDecision;
import br.com.escola.bff.application.service.CatalogReadRoute;
import br.com.escola.bff.infra.config.CatalogReadCutoverProperties;

@Component
public class CatalogReadCutoverDecider implements CatalogReadCutoverPolicyPort {

    private final CatalogReadCutoverProperties properties;
    private final CatalogMigrationReportGate reportGate;

    public CatalogReadCutoverDecider(
            CatalogReadCutoverProperties properties,
            CatalogMigrationReportGate reportGate) {
        this.properties = properties;
        this.reportGate = reportGate;
    }

    @Override
    public CatalogReadCutoverDecision decision(CatalogReadRoute route) {
        if (!properties.enabled()) {
            return new CatalogReadCutoverDecision(route, false, "cutover_disabled");
        }
        if (!properties.routeEnabled(route)) {
            return new CatalogReadCutoverDecision(route, false, "route_disabled");
        }

        CatalogMigrationReportGate.GateStatus gateStatus = reportGate.status();
        if (!gateStatus.allowed()) {
            return new CatalogReadCutoverDecision(route, false, gateStatus.reason());
        }

        return new CatalogReadCutoverDecision(route, true, "catalog_enabled");
    }

    @Override
    public boolean fallbackToMonolithOnError() {
        return properties.fallbackToMonolithOnError();
    }
}
