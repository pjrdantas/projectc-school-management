package br.com.escola.institutionaltenantservice.infra.persistence;

public record CargaEscolaReport(
        int copiedRows,
        int sourceRows,
        int targetRows,
        boolean reconciled) {
}
