package br.com.escola.planningaiservice.infra.migration;

import java.util.Map;

public record PlanejamentoBackfillReport(Map<String, Integer> sourceRows, Map<String, Integer> reconciledRows, boolean reconciled) {
}
