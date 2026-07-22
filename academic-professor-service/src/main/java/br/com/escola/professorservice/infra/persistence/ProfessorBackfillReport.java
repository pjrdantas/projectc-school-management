package br.com.escola.professorservice.infra.persistence;

import java.util.Map;

public record ProfessorBackfillReport(
        Map<String, Integer> copiedRows,
        Map<String, Integer> sourceRows,
        Map<String, Integer> reconciledRows,
        boolean reconciled) {
}
