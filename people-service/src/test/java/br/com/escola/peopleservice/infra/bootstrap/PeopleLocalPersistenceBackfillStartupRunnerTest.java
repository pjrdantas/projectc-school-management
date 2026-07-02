package br.com.escola.peopleservice.infra.bootstrap;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

import br.com.escola.peopleservice.application.service.PeopleLocalPersistenceBackfillCoordinator;
import br.com.escola.peopleservice.infra.config.PeopleLocalPersistenceProperties;

class PeopleLocalPersistenceBackfillStartupRunnerTest {

    @Test
    void naoExecutaCicloQuandoBackfillEReconciliacaoEstaoDesligados() {
        PeopleLocalPersistenceBackfillCoordinator coordinator = org.mockito.Mockito.mock(
                PeopleLocalPersistenceBackfillCoordinator.class);
        PeopleLocalPersistenceBackfillStartupRunner runner = new PeopleLocalPersistenceBackfillStartupRunner(
                new PeopleLocalPersistenceProperties(false, false, false, false, false, false, 500),
                coordinator);

        runner.run(new DefaultApplicationArguments());

        verify(coordinator, never()).executarCicloControlado();
    }

    @Test
    void executaCicloQuandoBackfillOuReconciliacaoEstaoLigados() {
        PeopleLocalPersistenceBackfillCoordinator coordinator = org.mockito.Mockito.mock(
                PeopleLocalPersistenceBackfillCoordinator.class);
        PeopleLocalPersistenceBackfillStartupRunner runner = new PeopleLocalPersistenceBackfillStartupRunner(
                new PeopleLocalPersistenceProperties(false, false, false, false, true, false, 500),
                coordinator);

        runner.run(new DefaultApplicationArguments());

        verify(coordinator).executarCicloControlado();
    }
}
