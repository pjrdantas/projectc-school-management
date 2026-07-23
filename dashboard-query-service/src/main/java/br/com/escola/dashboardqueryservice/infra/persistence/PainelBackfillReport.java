package br.com.escola.dashboardqueryservice.infra.persistence;

import java.util.Map;

public record PainelBackfillReport(
        Map<String, Integer> sourceRows,
        Map<String, Integer> reconciledRows,
        boolean reconciled) {
}
