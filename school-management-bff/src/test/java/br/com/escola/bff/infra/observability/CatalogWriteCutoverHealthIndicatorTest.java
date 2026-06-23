package br.com.escola.bff.infra.observability;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import br.com.escola.bff.infra.config.CatalogReadCutoverProperties;
import br.com.escola.bff.infra.config.CatalogServiceClientProperties;
import br.com.escola.bff.infra.config.CatalogWriteCutoverProperties;
import br.com.escola.bff.infra.cutover.CatalogMigrationReportGate;

class CatalogWriteCutoverHealthIndicatorTest {

    @Test
    void deveFicarUpQuandoCutoverEscritaEstiverDesabilitado() throws Exception {
        CatalogWriteCutoverProperties properties = new CatalogWriteCutoverProperties(
                false,
                new CatalogWriteCutoverProperties.RouteFlags(false, false, false, false, false));
        CatalogServiceClientProperties clientProperties = new CatalogServiceClientProperties(
                java.net.URI.create("http://localhost:8082"),
                java.time.Duration.ofSeconds(2),
                java.time.Duration.ofSeconds(3),
                "token");
        CatalogMigrationReportGate gate = gateComRelatorioValido();

        CatalogWriteCutoverHealthIndicator indicator =
                new CatalogWriteCutoverHealthIndicator(properties, clientProperties, gate);

        assertThat(indicator.health().getStatus().getCode()).isEqualTo("UP");
    }

    @Test
    void deveFicarDownQuandoCutoverEscritaEstiverHabilitadoSemRelatorioValido() {
        CatalogWriteCutoverProperties properties = new CatalogWriteCutoverProperties(
                true,
                new CatalogWriteCutoverProperties.RouteFlags(true, true, true, true, true));
        CatalogServiceClientProperties clientProperties = new CatalogServiceClientProperties(
                java.net.URI.create("http://localhost:8082"),
                java.time.Duration.ofSeconds(2),
                java.time.Duration.ofSeconds(3),
                "token");
        CatalogMigrationReportGate gate = new CatalogMigrationReportGate(
                new CatalogReadCutoverProperties(
                        true,
                        "",
                        true,
                        new CatalogReadCutoverProperties.RouteFlags(
                                false, false, false, false, false, false, false, false, false, false, false, false, false)),
                new com.fasterxml.jackson.databind.ObjectMapper().findAndRegisterModules());

        CatalogWriteCutoverHealthIndicator indicator =
                new CatalogWriteCutoverHealthIndicator(properties, clientProperties, gate);

        assertThat(indicator.health().getStatus().getCode()).isEqualTo("DOWN");
        assertThat(indicator.health().getDetails()).containsEntry("gateReason", "report_path_missing");
    }

    private CatalogMigrationReportGate gateComRelatorioValido() throws Exception {
        Path report = Files.createTempFile("catalog-write-health", ".json");
        Files.writeString(report, """
                {
                  "applied": true,
                  "reconciled": true,
                  "sourceIssues": [],
                  "targetIssues": [],
                  "tables": [
                    { "missingIds": [], "unexpectedIds": [], "divergentIds": [] }
                  ]
                }
                """);
        return new CatalogMigrationReportGate(
                new CatalogReadCutoverProperties(
                        true,
                        report.toString(),
                        true,
                        new CatalogReadCutoverProperties.RouteFlags(
                                false, false, false, false, false, false, false, false, false, false, false, false, false)),
                new com.fasterxml.jackson.databind.ObjectMapper().findAndRegisterModules());
    }
}
