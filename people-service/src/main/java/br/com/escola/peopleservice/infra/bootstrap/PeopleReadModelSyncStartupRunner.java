package br.com.escola.peopleservice.infra.bootstrap;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import br.com.escola.peopleservice.application.service.PeopleReadModelSyncCoordinator;
import br.com.escola.peopleservice.infra.config.PeopleReadModelProperties;

@Component
public class PeopleReadModelSyncStartupRunner implements ApplicationRunner {

    private final PeopleReadModelProperties properties;
    private final PeopleReadModelSyncCoordinator coordinator;

    public PeopleReadModelSyncStartupRunner(
            PeopleReadModelProperties properties,
            PeopleReadModelSyncCoordinator coordinator) {
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

