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
        if (!StringUtils.hasText(properties.reportPath())) {
            return false;
        }

        Path reportPath = Path.of(properties.reportPath()).toAbsolutePath().normalize();
        if (!Files.isRegularFile(reportPath)) {
            return false;
        }

        try {
            Instant lastModified = Files.getLastModifiedTime(reportPath).toInstant();
            CacheEntry current = cacheEntry;
            if (current != null
                    && current.path().equals(reportPath)
                    && current.lastModified().equals(lastModified)) {
                return current.allowed();
            }

            boolean allowed = evaluate(reportPath);
            cacheEntry = new CacheEntry(reportPath, lastModified, allowed);
            return allowed;
        } catch (IOException exception) {
            return false;
        }
    }

    private boolean evaluate(Path reportPath) throws IOException {
        MigrationReportSnapshot report = objectMapper.readValue(reportPath.toFile(), MigrationReportSnapshot.class);
        return report.applied()
                && report.reconciled()
                && report.sourceIssues().isEmpty()
                && report.targetIssues().isEmpty()
                && report.tables().stream().allMatch(MigrationTableSnapshot::reconciled);
    }

    private record CacheEntry(Path path, Instant lastModified, boolean allowed) {}

    private record MigrationReportSnapshot(
            boolean applied,
            boolean reconciled,
            List<String> sourceIssues,
            List<String> targetIssues,
            List<MigrationTableSnapshot> tables
    ) {
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
