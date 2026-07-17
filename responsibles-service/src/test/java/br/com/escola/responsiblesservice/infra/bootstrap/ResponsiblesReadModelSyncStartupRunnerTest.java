package br.com.escola.responsiblesservice.infra.bootstrap;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.escola.responsiblesservice.application.service.ResponsiblesReadModelSyncCoordinator;
import br.com.escola.responsiblesservice.application.state.ResponsiblesReadModelSyncState;
import br.com.escola.responsiblesservice.application.state.ResponsiblesReadModelSyncSummary;
import br.com.escola.responsiblesservice.infra.config.ResponsiblesReadModelProperties;

class ResponsiblesReadModelSyncStartupRunnerTest {

    @Test
    void naoExecutaBackfillQuandoFlagEstaDesligada() throws Exception {
        ResponsiblesReadModelSyncCoordinator coordinator = org.mockito.Mockito.mock(ResponsiblesReadModelSyncCoordinator.class);
        ResponsiblesReadModelSyncState syncState = org.mockito.Mockito.mock(ResponsiblesReadModelSyncState.class);
        ResponsiblesReadModelSyncStartupRunner runner = new ResponsiblesReadModelSyncStartupRunner(
                new ResponsiblesReadModelProperties(false, false, false, false, false, 500, false, true),
                coordinator,
                syncState);

        runner.run(null);

        verifyNoInteractions(coordinator);
        verifyNoInteractions(syncState);
    }

    @Test
    void executaBackfillQuandoFlagEstaLigada() throws Exception {
        ResponsiblesReadModelSyncCoordinator coordinator = org.mockito.Mockito.mock(ResponsiblesReadModelSyncCoordinator.class);
        ResponsiblesReadModelSyncState syncState = org.mockito.Mockito.mock(ResponsiblesReadModelSyncState.class);
        ResponsiblesReadModelSyncSummary summary = new ResponsiblesReadModelSyncSummary(
                true,
                false,
                "completed",
                "ok",
                100,
                0,
                0,
                0,
                0,
                0,
                0,
                List.of());
        org.mockito.Mockito.when(coordinator.executarCicloControlado()).thenReturn(summary);
        ResponsiblesReadModelSyncStartupRunner runner = new ResponsiblesReadModelSyncStartupRunner(
                new ResponsiblesReadModelProperties(true, false, false, true, false, 100, false, true),
                coordinator,
                syncState);

        runner.run(null);

        verify(coordinator).executarCicloControlado();
        verify(syncState).update(summary);
    }
}
