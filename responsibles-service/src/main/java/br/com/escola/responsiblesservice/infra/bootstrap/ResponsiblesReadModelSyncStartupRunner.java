package br.com.escola.responsiblesservice.infra.bootstrap;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import br.com.escola.responsiblesservice.application.service.ResponsiblesReadModelSyncCoordinator;
import br.com.escola.responsiblesservice.application.state.ResponsiblesReadModelSyncState;
import br.com.escola.responsiblesservice.infra.config.ResponsiblesReadModelProperties;

@Component
@Order(1)
public class ResponsiblesReadModelSyncStartupRunner implements ApplicationRunner {

    private final ResponsiblesReadModelProperties properties;
    private final ResponsiblesReadModelSyncCoordinator coordinator;
    private final ResponsiblesReadModelSyncState syncState;

    public ResponsiblesReadModelSyncStartupRunner(
            ResponsiblesReadModelProperties properties,
            ResponsiblesReadModelSyncCoordinator coordinator,
            ResponsiblesReadModelSyncState syncState) {
        this.properties = properties;
        this.coordinator = coordinator;
        this.syncState = syncState;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (properties.backfillEnabled() || properties.reconciliationEnabled()) {
            syncState.update(coordinator.executarCicloControlado());
        }
    }
}
