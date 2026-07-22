package br.com.escola.pedagogicalservice.infra.persistence;

public record HistoricoEscolarBackfillReport(int sourceRows, int copiedRows, int reconciledRows, boolean reconciled) {
}
