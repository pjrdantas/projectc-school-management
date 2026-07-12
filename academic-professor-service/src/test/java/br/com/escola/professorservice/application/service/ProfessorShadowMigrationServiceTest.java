package br.com.escola.professorservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.com.escola.professorservice.application.migration.ProfessorShadowMigrationSnapshot;
import br.com.escola.professorservice.application.port.out.ProfessorShadowMigrationSourcePort;
import br.com.escola.professorservice.application.port.out.ProfessorShadowMigrationTargetPort;

class ProfessorShadowMigrationServiceTest {

    @Mock private ProfessorShadowMigrationSourcePort sourcePort;
    @Mock private ProfessorShadowMigrationTargetPort targetPort;

    private ProfessorShadowMigrationService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ProfessorShadowMigrationService(
                sourcePort,
                targetPort,
                Clock.fixed(Instant.parse("2026-06-29T12:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void deveAplicarEReconciliarSnapshotDeProfessor() {
        ProfessorShadowMigrationSnapshot snapshot = snapshotValido();
        when(sourcePort.carregarSnapshot()).thenReturn(snapshot);
        when(targetPort.carregarSnapshot()).thenReturn(snapshot);

        var report = service.executar(true);

        assertThat(report.applied()).isTrue();
        assertThat(report.reconciled()).isTrue();
        assertThat(report.tables()).allMatch(table -> table.reconciled());
        verify(targetPort).aplicar(snapshot);
    }

    @Test
    void deveRecusarApplyQuandoOrigemPossuiDuplicidadePorPessoaEscola() {
        UUID escolaId = UUID.randomUUID();
        UUID pessoaId = UUID.randomUUID();
        ProfessorShadowMigrationSnapshot invalid = new ProfessorShadowMigrationSnapshot(List.of(
                row(UUID.randomUUID(), pessoaId, escolaId, "Professor A"),
                row(UUID.randomUUID(), pessoaId, escolaId, "Professor B")));
        when(sourcePort.carregarSnapshot()).thenReturn(invalid);
        when(targetPort.carregarSnapshot()).thenReturn(new ProfessorShadowMigrationSnapshot(List.of()));

        var report = service.executar(true);

        assertThat(report.applyRequested()).isTrue();
        assertThat(report.applied()).isFalse();
        assertThat(report.reconciled()).isFalse();
        assertThat(report.sourceIssues()).anyMatch(issue -> issue.contains("professor_por_pessoa_escola"));
        verify(targetPort, never()).aplicar(invalid);
    }

    @Test
    void deveDetectarDivergenciaEmDryRun() {
        UUID escolaId = UUID.randomUUID();
        UUID professorId = UUID.randomUUID();
        UUID pessoaId = UUID.randomUUID();
        ProfessorShadowMigrationSnapshot source = new ProfessorShadowMigrationSnapshot(List.of(
                row(professorId, pessoaId, escolaId, "Professor Correto")));
        ProfessorShadowMigrationSnapshot target = new ProfessorShadowMigrationSnapshot(List.of(
                new ProfessorShadowMigrationSnapshot.ProfessorRow(
                        professorId,
                        pessoaId,
                        escolaId,
                        "Escola Teste",
                        "Professor Divergente",
                        "RP-1",
                        "Licenciatura",
                        true,
                        LocalDateTime.of(2026, 6, 29, 9, 0),
                        LocalDateTime.of(2026, 6, 29, 9, 0),
                        null)));
        when(sourcePort.carregarSnapshot()).thenReturn(source);
        when(targetPort.carregarSnapshot()).thenReturn(target);

        var report = service.executar(false);

        assertThat(report.applied()).isFalse();
        assertThat(report.reconciled()).isFalse();
        assertThat(report.tables()).singleElement().satisfies(table ->
                assertThat(table.divergentIds()).containsExactly(professorId));
    }

    private ProfessorShadowMigrationSnapshot snapshotValido() {
        UUID escolaId = UUID.randomUUID();
        UUID pessoaId = UUID.randomUUID();
        UUID professorId = UUID.randomUUID();
        return new ProfessorShadowMigrationSnapshot(List.of(
                row(professorId, pessoaId, escolaId, "Professor Teste")));
    }

    private ProfessorShadowMigrationSnapshot.ProfessorRow row(
            UUID professorId,
            UUID pessoaId,
            UUID escolaId,
            String nomeCompleto) {
        return new ProfessorShadowMigrationSnapshot.ProfessorRow(
                professorId,
                pessoaId,
                escolaId,
                "Escola Teste",
                nomeCompleto,
                "RP-1",
                "Licenciatura",
                true,
                LocalDateTime.of(2026, 6, 29, 9, 0),
                LocalDateTime.of(2026, 6, 29, 9, 0),
                null);
    }
}
