package br.com.escola.peopleservice.infra.bootstrap;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import br.com.escola.peopleservice.application.service.LeituraModeloSyncCoordinator;
import br.com.escola.peopleservice.infra.config.LeituraModeloProperties;

@Component
public class LeituraModeloSyncStartupRunner implements ApplicationRunner {

    private final LeituraModeloProperties properties;
    private final LeituraModeloSyncCoordinator coordinator;

    public LeituraModeloSyncStartupRunner(
            LeituraModeloProperties properties,
            LeituraModeloSyncCoordinator coordinator) {
        this.properties = properties;
        this.coordinator = coordinator;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (properties.backfillEnabled() || properties.reconciliationEnabled()) {
            coordinator.executarCicloControlado();
        }
    }
}


