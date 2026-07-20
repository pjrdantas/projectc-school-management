package br.com.escola.identityaccessservice.infra.persistence;

import java.util.Map;

public record BackfillReport(
        Map<String, Integer> copiedRows,
        Map<String, Integer> sourceRows,
        Map<String, Integer> targetRows,
        boolean reconciled) {
}
