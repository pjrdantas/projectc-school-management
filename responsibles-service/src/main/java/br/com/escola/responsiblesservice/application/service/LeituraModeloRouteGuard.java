package br.com.escola.responsiblesservice.application.service;

import org.springframework.stereotype.Component;

import br.com.escola.responsiblesservice.application.state.LeituraModeloSyncState;
import br.com.escola.responsiblesservice.infra.config.LeituraModeloProperties;

@Component
public class LeituraModeloRouteGuard {

    private final LeituraModeloProperties properties;
    private final LeituraModeloSyncState syncState;

    public LeituraModeloRouteGuard(
            LeituraModeloProperties properties,
            LeituraModeloSyncState syncState) {
        this.properties = properties;
        this.syncState = syncState;
    }

    public boolean canReadStudentLinksLocally() {
        if (!properties.enabled() || !properties.localReadRoutingEnabled()) {
            return false;
        }
        if (!properties.reconciliationEnabled()) {
            return true;
        }
        return syncState.isLinkRouteReady();
    }

    public boolean canReadCatalogLocally() {
        if (!properties.enabled() || !properties.localReadRoutingEnabled()) {
            return false;
        }
        if (!properties.reconciliationEnabled()) {
            return true;
        }
        return syncState.isCatalogRouteReady();
    }
}

