package br.com.escola.professorservice.application.migration;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProfessorShadowMigrationReport(
        Instant startedAt,
        Instant finishedAt,
        boolean applyRequested,
        boolean applied,
        boolean reconciled,
        List<String> sourceIssues,
        List<String> targetIssues,
        List<TableReport> tables) {

    public ProfessorShadowMigrationReport {
        sourceIssues = List.copyOf(sourceIssues);
        targetIssues = List.copyOf(targetIssues);
        tables = List.copyOf(tables);
    }

    public record TableReport(
            String table,
            UUID escolaId,
            int sourceCount,
            int targetCount,
            List<UUID> missingIds,
            List<UUID> unexpectedIds,
            List<UUID> divergentIds) {

        public TableReport {
            missingIds = List.copyOf(missingIds);
            unexpectedIds = List.copyOf(unexpectedIds);
            divergentIds = List.copyOf(divergentIds);
        }

        public boolean reconciled() {
            return missingIds.isEmpty() && unexpectedIds.isEmpty() && divergentIds.isEmpty();
        }
    }
}
