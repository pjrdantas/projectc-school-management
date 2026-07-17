package br.com.escola.peopleservice.infra.bootstrap;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

import br.com.escola.peopleservice.application.service.LeituraModeloSyncCoordinator;
import br.com.escola.peopleservice.infra.config.LeituraModeloProperties;

class LeituraModeloSyncStartupRunnerTest {

    @Test
    void naoExecutaCicloQuandoBackfillEReconciliacaoEstaoDesligados() {
        LeituraModeloSyncCoordinator coordinator = org.mockito.Mockito.mock(
                LeituraModeloSyncCoordinator.class);
        LeituraModeloSyncStartupRunner runner = new LeituraModeloSyncStartupRunner(
                new LeituraModeloProperties(false, false, false, false, false, false, 500, true),
                coordinator);

        runner.run(new DefaultApplicationArguments());

        verify(coordinator, never()).executarCicloControlado();
    }

    @Test
    void executaCicloQuandoBackfillOuReconciliacaoEstaoLigados() {
        LeituraModeloSyncCoordinator coordinator = org.mockito.Mockito.mock(
                LeituraModeloSyncCoordinator.class);
        LeituraModeloSyncStartupRunner runner = new LeituraModeloSyncStartupRunner(
                new LeituraModeloProperties(false, false, false, false, true, false, 500, true),
                coordinator);

        runner.run(new DefaultApplicationArguments());

        verify(coordinator).executarCicloControlado();
    }
}


