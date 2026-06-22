package br.com.escola.bff.infra.cutover;

import org.springframework.stereotype.Component;

import br.com.escola.bff.application.port.out.CatalogReadCutoverPolicyPort;
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
    public boolean shouldUseCatalog(CatalogReadRoute route) {
        return properties.enabled()
                && properties.routeEnabled(route)
                && reportGate.allowsCutover();
    }

    @Override
    public boolean fallbackToMonolithOnError() {
        return properties.fallbackToMonolithOnError();
    }
}
