package br.com.escola.peopleservice.infra.bootstrap;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import br.com.escola.peopleservice.application.service.PeopleLocalPersistenceBackfillCoordinator;
import br.com.escola.peopleservice.infra.config.PeopleLocalPersistenceProperties;

@Component
public class PeopleLocalPersistenceBackfillStartupRunner implements ApplicationRunner {

    private final PeopleLocalPersistenceProperties properties;
    private final PeopleLocalPersistenceBackfillCoordinator coordinator;

    public PeopleLocalPersistenceBackfillStartupRunner(
            PeopleLocalPersistenceProperties properties,
            PeopleLocalPersistenceBackfillCoordinator coordinator) {
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
