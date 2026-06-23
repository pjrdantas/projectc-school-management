package br.com.escola.bff.infra.observability;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.escola.bff.infra.config.CatalogReadCutoverProperties;
import br.com.escola.bff.infra.cutover.CatalogMigrationReportGate;

class CatalogReadCutoverHealthIndicatorTest {

    @Test
    void deveFicarUpQuandoCutoverEstiverDesabilitadoMesmoSemRelatorio() {
        CatalogReadCutoverProperties properties = new CatalogReadCutoverProperties(
                false, "", true,
                new CatalogReadCutoverProperties.RouteFlags(
                        false, false, false, false, false, false, false, false, false, false, false, false, false));
        CatalogMigrationReportGate gate = new CatalogMigrationReportGate(properties, new ObjectMapper());

        CatalogReadCutoverHealthIndicator indicator = new CatalogReadCutoverHealthIndicator(properties, gate);

        assertThat(indicator.health().getStatus().getCode()).isEqualTo("UP");
    }

    @Test
    void deveFicarDownQuandoCutoverEstiverHabilitadoSemRelatorioValido() {
        CatalogReadCutoverProperties properties = new CatalogReadCutoverProperties(
                true, "", true,
                new CatalogReadCutoverProperties.RouteFlags(
                        false, false, false, false, false, false, false, false, false, false, false, false, false));
        CatalogMigrationReportGate gate = new CatalogMigrationReportGate(properties, new ObjectMapper());

        CatalogReadCutoverHealthIndicator indicator = new CatalogReadCutoverHealthIndicator(properties, gate);

        assertThat(indicator.health().getStatus().getCode()).isEqualTo("DOWN");
        assertThat(indicator.health().getDetails()).containsEntry("gateReason", "report_path_missing");
    }

    @Test
    void deveFicarUpQuandoRelatorioEstiverReconciliado() throws Exception {
        Path report = Files.createTempFile("catalog-cutover-health", ".json");
        Files.writeString(report, """
                {
                  "applied": true,
                  "reconciled": true,
                  "sourceIssues": [],
                  "targetIssues": [],
                  "tables": [
                    {
                      "missingIds": [],
                      "unexpectedIds": [],
                      "divergentIds": []
                    }
                  ]
                }
                """);
        try {
            CatalogReadCutoverProperties properties = new CatalogReadCutoverProperties(
                    true, report.toString(), true,
                    new CatalogReadCutoverProperties.RouteFlags(
                            false, false, false, false, false, false, false, false, false, false, false, false, false));
            CatalogMigrationReportGate gate = new CatalogMigrationReportGate(properties, new ObjectMapper());

            CatalogReadCutoverHealthIndicator indicator = new CatalogReadCutoverHealthIndicator(properties, gate);

            assertThat(indicator.health().getStatus().getCode()).isEqualTo("UP");
            assertThat(indicator.health().getDetails()).containsEntry("gateAllowed", true);
        } finally {
            Files.deleteIfExists(report);
        }
    }
}
