package br.com.escola.catalog.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.com.escola.catalog.application.migration.MigracaoSnapshot;
import br.com.escola.catalog.application.port.out.MigracaoOrigemPort;
import br.com.escola.catalog.application.port.out.MigracaoDestinoPort;
import br.com.escola.catalog.application.port.out.LeituraCachePort;

class MigracaoServiceTest {

    @Mock private MigracaoOrigemPort sourcePort;
    @Mock private MigracaoDestinoPort targetPort;
    @Mock private LeituraCachePort cachePort;

    private MigracaoService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new MigracaoService(
                sourcePort, targetPort, cachePort,
                Clock.fixed(Instant.parse("2026-06-22T15:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void deveAplicarEReconciliarSnapshotRepetivel() {
        MigracaoSnapshot snapshot = validSnapshot();
        when(sourcePort.carregarSnapshot()).thenReturn(snapshot);
        when(targetPort.carregarSnapshot()).thenReturn(snapshot);

        var report = service.executar(true);

        assertThat(report.applied()).isTrue();
        assertThat(report.reconciled()).isTrue();
        assertThat(report.tables()).allMatch(table -> table.reconciled());
        verify(targetPort).aplicar(snapshot);
    }

    @Test
    void deveRecusarApplyQuandoOrigemViolaContratoDoDestino() {
        UUID escola = UUID.randomUUID();
        var invalid = new MigracaoSnapshot(
                List.of(), List.of(), List.of(),
                List.of(new MigracaoSnapshot.SerieRow(
                        UUID.randomUUID(), escola, "1 ano", 1, null, java.time.LocalDateTime.now())),
                List.of(), List.of(), List.of());
        when(sourcePort.carregarSnapshot()).thenReturn(invalid);
        when(targetPort.carregarSnapshot()).thenReturn(emptySnapshot());

        var report = service.executar(true);

        assertThat(report.applyRequested()).isTrue();
        assertThat(report.applied()).isFalse();
        assertThat(report.reconciled()).isFalse();
        assertThat(report.sourceIssues()).anyMatch(issue -> issue.contains("nivel_ensino_invalido"));
        verify(targetPort, never()).aplicar(invalid);
    }

    @Test
    void devePermitirSubstituirSeedsGlobaisQuandoDestinoAindaNaoPossuiDadosEscolares() {
        var source = validSnapshot();
        var targetWithSeedIds = new MigracaoSnapshot(
                List.of(new MigracaoSnapshot.NivelEnsinoRow(
                        UUID.randomUUID(), "FUNDAMENTAL", "Fundamental")),
                List.of(new MigracaoSnapshot.TurnoRow(
                        UUID.randomUUID(), "MANHA", "Manha")),
                List.of(), List.of(), List.of(), List.of(), List.of());
        when(sourcePort.carregarSnapshot()).thenReturn(source);
        when(targetPort.carregarSnapshot()).thenReturn(targetWithSeedIds, source);

        var report = service.executar(true);

        assertThat(report.applied()).isTrue();
        assertThat(report.targetIssues()).isEmpty();
        verify(targetPort).aplicar(source);
    }

    private MigracaoSnapshot validSnapshot() {
        UUID nivel = UUID.randomUUID();
        UUID turno = UUID.randomUUID();
        return new MigracaoSnapshot(
                List.of(new MigracaoSnapshot.NivelEnsinoRow(nivel, "FUNDAMENTAL", "Fundamental")),
                List.of(new MigracaoSnapshot.TurnoRow(turno, "MANHA", "Manha")),
                List.of(), List.of(), List.of(), List.of(), List.of());
    }

    private MigracaoSnapshot emptySnapshot() {
        return new MigracaoSnapshot(
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
    }
}

