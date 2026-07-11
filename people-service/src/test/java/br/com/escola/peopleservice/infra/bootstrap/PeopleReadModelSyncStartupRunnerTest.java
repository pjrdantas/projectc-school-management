package br.com.escola.peopleservice.infra.bootstrap;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

import br.com.escola.peopleservice.application.service.PeopleReadModelSyncCoordinator;
import br.com.escola.peopleservice.infra.config.PeopleReadModelProperties;

class PeopleReadModelSyncStartupRunnerTest {

    @Test
    void naoExecutaCicloQuandoBackfillEReconciliacaoEstaoDesligados() {
        PeopleReadModelSyncCoordinator coordinator = org.mockito.Mockito.mock(
                PeopleReadModelSyncCoordinator.class);
        PeopleReadModelSyncStartupRunner runner = new PeopleReadModelSyncStartupRunner(
                new PeopleReadModelProperties(false, false, false, false, false, false, 500, true),
                coordinator);

        runner.run(new DefaultApplicationArguments());

        verify(coordinator, never()).executarCicloControlado();
    }

    @Test
    void executaCicloQuandoBackfillOuReconciliacaoEstaoLigados() {
        PeopleReadModelSyncCoordinator coordinator = org.mockito.Mockito.mock(
                PeopleReadModelSyncCoordinator.class);
        PeopleReadModelSyncStartupRunner runner = new PeopleReadModelSyncStartupRunner(
                new PeopleReadModelProperties(false, false, false, false, true, false, 500, true),
                coordinator);

        runner.run(new DefaultApplicationArguments());

        verify(coordinator).executarCicloControlado();
    }
}

