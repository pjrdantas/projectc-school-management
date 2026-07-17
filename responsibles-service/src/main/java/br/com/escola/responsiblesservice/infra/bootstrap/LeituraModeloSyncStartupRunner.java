package br.com.escola.responsiblesservice.infra.bootstrap;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import br.com.escola.responsiblesservice.application.service.LeituraModeloSyncCoordinator;
import br.com.escola.responsiblesservice.application.state.LeituraModeloSyncState;
import br.com.escola.responsiblesservice.infra.config.LeituraModeloProperties;

@Component
@Order(1)
public class LeituraModeloSyncStartupRunner implements ApplicationRunner {

    private final LeituraModeloProperties properties;
    private final LeituraModeloSyncCoordinator coordinator;
    private final LeituraModeloSyncState syncState;

    public LeituraModeloSyncStartupRunner(
            LeituraModeloProperties properties,
            LeituraModeloSyncCoordinator coordinator,
            LeituraModeloSyncState syncState) {
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

