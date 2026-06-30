package br.com.escola.bff.infra.cutover;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.escola.bff.infra.config.CatalogReadCutoverProperties;

@Component
public class CatalogMigrationReportGate {

    private final CatalogReadCutoverProperties properties;
    private final ObjectMapper objectMapper;

    private volatile CacheEntry cacheEntry;

    public CatalogMigrationReportGate(
            CatalogReadCutoverProperties properties,
            ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public boolean allowsCutover() {
        return status().allowed();
    }

    public GateStatus status() {
        if (!StringUtils.hasText(properties.reportPath())) {
            return new GateStatus(false, "report_path_missing", null, false, false, null);
        }

        Path reportPath = Path.of(properties.reportPath()).toAbsolutePath().normalize();
        if (!Files.isRegularFile(reportPath)) {
            return new GateStatus(false, "report_file_missing", reportPath, false, false, null);
        }

        try {
            Instant lastModified = Files.getLastModifiedTime(reportPath).toInstant();
            CacheEntry current = cacheEntry;
            if (current != null
                    && current.path().equals(reportPath)
                    && current.lastModified().equals(lastModified)) {
                return current.status();
            }

            GateStatus status = evaluate(reportPath, lastModified);
            cacheEntry = new CacheEntry(reportPath, lastModified, status);
            return status;
        } catch (IOException exception) {
            return new GateStatus(false, "report_unreadable", reportPath, true, false, null);
        }
    }

    private GateStatus evaluate(Path reportPath, Instant lastModified) throws IOException {
        MigrationReportSnapshot report = objectMapper.readValue(reportPath.toFile(), MigrationReportSnapshot.class);
        boolean allowed = report.applied()
                && report.reconciled()
                && report.sourceIssues().isEmpty()
                && report.targetIssues().isEmpty()
                && report.tables().stream().allMatch(MigrationTableSnapshot::reconciled);
        return new GateStatus(
                allowed,
                allowed ? "allowed" : "report_not_reconciled",
                reportPath,
                true,
                true,
                lastModified);
    }

    public record GateStatus(
            boolean allowed,
            String reason,
            Path reportPath,
            boolean reportFilePresent,
            boolean reportParsed,
            Instant lastModified
    ) {}

    private record CacheEntry(Path path, Instant lastModified, GateStatus status) {}

    private record MigrationReportSnapshot(
            boolean applied,
            boolean reconciled,
            List<String> sourceIssues,
            List<String> targetIssues,
            List<MigrationTableSnapshot> tables
    ) {
        @SuppressWarnings("unused")
		MigrationReportSnapshot {
            sourceIssues = sourceIssues == null ? List.of() : List.copyOf(sourceIssues);
            targetIssues = targetIssues == null ? List.of() : List.copyOf(targetIssues);
            tables = tables == null ? List.of() : List.copyOf(tables);
        }
    }

    private record MigrationTableSnapshot(
            List<String> missingIds,
            List<String> unexpectedIds,
            List<String> divergentIds
    ) {
        @SuppressWarnings("unused")
		MigrationTableSnapshot {
            missingIds = missingIds == null ? List.of() : List.copyOf(missingIds);
            unexpectedIds = unexpectedIds == null ? List.of() : List.copyOf(unexpectedIds);
            divergentIds = divergentIds == null ? List.of() : List.copyOf(divergentIds);
        }

        boolean reconciled() {
            return missingIds.isEmpty() && unexpectedIds.isEmpty() && divergentIds.isEmpty();
        }
    }
}
