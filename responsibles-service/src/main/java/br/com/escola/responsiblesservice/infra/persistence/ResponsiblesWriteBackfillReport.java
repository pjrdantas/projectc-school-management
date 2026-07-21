package br.com.escola.responsiblesservice.infra.persistence;

import java.util.Map;

public record ResponsiblesWriteBackfillReport(
        Map<String, Integer> copiedRows,
        Map<String, Integer> sourceRows,
        Map<String, Integer> reconciledRows,
        boolean reconciled) {
}
