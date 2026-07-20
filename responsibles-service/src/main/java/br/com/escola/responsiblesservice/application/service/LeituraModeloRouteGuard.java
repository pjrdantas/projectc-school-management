package br.com.escola.responsiblesservice.application.service;

import org.springframework.stereotype.Component;

import br.com.escola.responsiblesservice.infra.config.LeituraModeloProperties;

@Component
public class LeituraModeloRouteGuard {

    private final LeituraModeloProperties properties;
    public LeituraModeloRouteGuard(LeituraModeloProperties properties) {
        this.properties = properties;
    }

    public boolean canReadStudentLinksLocally() {
        if (!properties.enabled() || !properties.localReadRoutingEnabled()) {
            return false;
        }
        return !properties.fallbackEnabled();
    }

    public boolean canReadCatalogLocally() {
        if (!properties.enabled() || !properties.localReadRoutingEnabled()) {
            return false;
        }
        return !properties.fallbackEnabled();
    }
}

