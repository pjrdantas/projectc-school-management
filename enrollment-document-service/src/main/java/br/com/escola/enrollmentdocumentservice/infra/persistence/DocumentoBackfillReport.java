package br.com.escola.enrollmentdocumentservice.infra.persistence;

import java.util.Map;

public record DocumentoBackfillReport(
        Map<String, Integer> copiedRows,
        Map<String, Integer> sourceRows,
        Map<String, Integer> reconciledRows,
        boolean reconciled) {
}
