package br.com.escola.bff.infra.cutover;

import org.springframework.stereotype.Component;

import br.com.escola.bff.application.port.out.CatalogWriteCutoverPolicyPort;
import br.com.escola.bff.application.service.CatalogWriteCutoverDecision;
import br.com.escola.bff.application.service.CatalogWriteRoute;
import br.com.escola.bff.infra.config.CatalogWriteCutoverProperties;

@Component
public class CatalogWriteCutoverDecider implements CatalogWriteCutoverPolicyPort {

    private final CatalogWriteCutoverProperties properties;
    private final CatalogMigrationReportGate reportGate;

    public CatalogWriteCutoverDecider(
            CatalogWriteCutoverProperties properties,
            CatalogMigrationReportGate reportGate) {
        this.properties = properties;
        this.reportGate = reportGate;
    }

    @Override
    public CatalogWriteCutoverDecision decision(CatalogWriteRoute route) {
        if (!properties.enabled()) {
            return new CatalogWriteCutoverDecision(route, false, "cutover_disabled");
        }
        if (!properties.routeEnabled(route)) {
            return new CatalogWriteCutoverDecision(route, false, "route_disabled");
        }
        CatalogMigrationReportGate.GateStatus gateStatus = reportGate.status();
        if (!gateStatus.allowed()) {
            return new CatalogWriteCutoverDecision(route, false, gateStatus.reason());
        }
        return new CatalogWriteCutoverDecision(route, true, "catalog_enabled");
    }
}
