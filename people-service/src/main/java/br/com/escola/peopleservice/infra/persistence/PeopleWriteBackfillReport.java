package br.com.escola.peopleservice.infra.persistence;

import java.util.Map;

public record PeopleWriteBackfillReport(
        Map<String, Integer> copiedRows,
        Map<String, Integer> sourceRows,
        Map<String, Integer> reconciledRows,
        boolean reconciled) {
}
