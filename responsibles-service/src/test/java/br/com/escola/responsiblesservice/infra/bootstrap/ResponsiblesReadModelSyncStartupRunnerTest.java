package br.com.escola.responsiblesservice.infra.bootstrap;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;

import br.com.escola.responsiblesservice.application.service.ResponsiblesReadModelSyncCoordinator;
import br.com.escola.responsiblesservice.infra.config.ResponsiblesReadModelProperties;

class ResponsiblesReadModelSyncStartupRunnerTest {

    @Test
    void naoExecutaBackfillQuandoFlagEstaDesligada() throws Exception {
        ResponsiblesReadModelSyncCoordinator coordinator = org.mockito.Mockito.mock(ResponsiblesReadModelSyncCoordinator.class);
        ResponsiblesReadModelSyncStartupRunner runner = new ResponsiblesReadModelSyncStartupRunner(
                new ResponsiblesReadModelProperties(false, false, false, false, 500, false, true),
                coordinator);

        runner.run(null);

        verifyNoInteractions(coordinator);
    }

    @Test
    void executaBackfillQuandoFlagEstaLigada() throws Exception {
        ResponsiblesReadModelSyncCoordinator coordinator = org.mockito.Mockito.mock(ResponsiblesReadModelSyncCoordinator.class);
        ResponsiblesReadModelSyncStartupRunner runner = new ResponsiblesReadModelSyncStartupRunner(
                new ResponsiblesReadModelProperties(true, false, false, true, 100, false, true),
                coordinator);

        runner.run(null);

        verify(coordinator).executarCicloControlado();
    }
}
