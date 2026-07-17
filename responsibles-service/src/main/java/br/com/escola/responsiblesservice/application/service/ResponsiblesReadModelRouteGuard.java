package br.com.escola.responsiblesservice.application.service;

import org.springframework.stereotype.Component;

import br.com.escola.responsiblesservice.application.state.ResponsiblesReadModelSyncState;
import br.com.escola.responsiblesservice.infra.config.ResponsiblesReadModelProperties;

@Component
public class ResponsiblesReadModelRouteGuard {

    private final ResponsiblesReadModelProperties properties;
    private final ResponsiblesReadModelSyncState syncState;

    public ResponsiblesReadModelRouteGuard(
            ResponsiblesReadModelProperties properties,
            ResponsiblesReadModelSyncState syncState) {
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
}
