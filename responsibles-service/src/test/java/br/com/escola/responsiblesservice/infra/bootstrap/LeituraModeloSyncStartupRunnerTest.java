package br.com.escola.responsiblesservice.infra.bootstrap;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.escola.responsiblesservice.application.service.LeituraModeloSyncCoordinator;
import br.com.escola.responsiblesservice.application.state.LeituraModeloSyncState;
import br.com.escola.responsiblesservice.application.state.LeituraModeloSyncSummary;
import br.com.escola.responsiblesservice.infra.config.LeituraModeloProperties;

class LeituraModeloSyncStartupRunnerTest {

    @Test
    void naoExecutaBackfillQuandoFlagEstaDesligada() throws Exception {
        LeituraModeloSyncCoordinator coordinator = org.mockito.Mockito.mock(LeituraModeloSyncCoordinator.class);
        LeituraModeloSyncState syncState = org.mockito.Mockito.mock(LeituraModeloSyncState.class);
        LeituraModeloSyncStartupRunner runner = new LeituraModeloSyncStartupRunner(
                new LeituraModeloProperties(false, false, false, false, false, 500, false, true),
                coordinator,
                syncState);

        runner.run(null);

        verifyNoInteractions(coordinator);
        verifyNoInteractions(syncState);
    }

    @Test
    void executaBackfillQuandoFlagEstaLigada() throws Exception {
        LeituraModeloSyncCoordinator coordinator = org.mockito.Mockito.mock(LeituraModeloSyncCoordinator.class);
        LeituraModeloSyncState syncState = org.mockito.Mockito.mock(LeituraModeloSyncState.class);
        LeituraModeloSyncSummary summary = new LeituraModeloSyncSummary(
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
        LeituraModeloSyncStartupRunner runner = new LeituraModeloSyncStartupRunner(
                new LeituraModeloProperties(true, false, false, true, false, 100, false, true),
                coordinator,
                syncState);

        runner.run(null);

        verify(coordinator).executarCicloControlado();
        verify(syncState).update(summary);
    }
}

