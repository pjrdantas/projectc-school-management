package br.com.escola.responsiblesservice.infra.bootstrap;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import br.com.escola.responsiblesservice.application.service.ResponsiblesReadModelSyncCoordinator;
import br.com.escola.responsiblesservice.infra.config.ResponsiblesReadModelProperties;

@Component
public class ResponsiblesReadModelSyncStartupRunner implements ApplicationRunner {

    private final ResponsiblesReadModelProperties properties;
    private final ResponsiblesReadModelSyncCoordinator coordinator;

    public ResponsiblesReadModelSyncStartupRunner(
            ResponsiblesReadModelProperties properties,
            ResponsiblesReadModelSyncCoordinator coordinator) {
        this.properties = properties;
        this.coordinator = coordinator;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (properties.backfillEnabled()) {
            coordinator.executarCicloControlado();
        }
    }
}
