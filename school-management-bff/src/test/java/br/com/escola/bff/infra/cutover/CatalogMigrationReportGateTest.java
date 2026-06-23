package br.com.escola.bff.infra.cutover;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.escola.bff.infra.config.CatalogReadCutoverProperties;

class CatalogMigrationReportGateTest {

    @Test
    void deveBloquearQuandoCaminhoDoRelatorioNaoEstiverConfigurado() {
        CatalogMigrationReportGate gate = new CatalogMigrationReportGate(
                new CatalogReadCutoverProperties(true, "", true, new CatalogReadCutoverProperties.RouteFlags(
                        false, false, false, false, false, false, false, false, false, false, false, false, false)),
                new ObjectMapper());

        CatalogMigrationReportGate.GateStatus status = gate.status();

        assertThat(status.allowed()).isFalse();
        assertThat(status.reason()).isEqualTo("report_path_missing");
    }

    @Test
    void devePermitirQuandoRelatorioEstiverReconciliado() throws Exception {
        Path report = Files.createTempFile("catalog-cutover", ".json");
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
            CatalogMigrationReportGate gate = new CatalogMigrationReportGate(
                    new CatalogReadCutoverProperties(true, report.toString(), true, new CatalogReadCutoverProperties.RouteFlags(
                            false, false, false, false, false, false, false, false, false, false, false, false, false)),
                    new ObjectMapper());

            CatalogMigrationReportGate.GateStatus status = gate.status();

            assertThat(status.allowed()).isTrue();
            assertThat(status.reason()).isEqualTo("allowed");
            assertThat(status.reportFilePresent()).isTrue();
            assertThat(status.reportParsed()).isTrue();
        } finally {
            Files.deleteIfExists(report);
        }
    }
}
