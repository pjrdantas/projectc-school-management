package br.com.escola.catalog.infra.migration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.escola.catalog.application.migration.CatalogMigrationReport;
import br.com.escola.catalog.application.service.CatalogMigrationService;

@Component
@ConditionalOnProperty(
        name = {"catalog.migration.enabled", "catalog.migration.runner-enabled"},
        havingValue = "true")
public class CatalogMigrationRunner implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogMigrationRunner.class);

    private final CatalogMigrationService service;
    private final ObjectMapper objectMapper;
    private final boolean apply;
    private final Path reportPath;

    public CatalogMigrationRunner(
            CatalogMigrationService service,
            ObjectMapper objectMapper,
            @Value("${catalog.migration.apply:false}") boolean apply,
            @Value("${catalog.migration.report-path:target/catalog-migration-report.json}") String reportPath) {
        this.service = service;
        this.objectMapper = objectMapper;
        this.apply = apply;
        this.reportPath = Path.of(reportPath).toAbsolutePath().normalize();
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        CatalogMigrationReport report = service.executar(apply);
        writeAtomically(report);
        if (report.reconciled()) {
            LOGGER.info("Migracao do catalogo reconciliada; apply={} report={}", apply, reportPath);
        } else {
            LOGGER.warn("Migracao do catalogo possui divergencias; apply={} sourceIssues={} targetIssues={} report={}",
                    apply, report.sourceIssues().size(), report.targetIssues().size(), reportPath);
        }
    }

    private void writeAtomically(CatalogMigrationReport report) throws IOException {
        Path parent = reportPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path temporary = reportPath.resolveSibling(reportPath.getFileName() + ".tmp");
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(temporary.toFile(), report);
        try {
            Files.move(temporary, reportPath, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (java.nio.file.AtomicMoveNotSupportedException exception) {
            Files.move(temporary, reportPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
