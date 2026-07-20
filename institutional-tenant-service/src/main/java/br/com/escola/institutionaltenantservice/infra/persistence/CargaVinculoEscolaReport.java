package br.com.escola.institutionaltenantservice.infra.persistence;

public record CargaVinculoEscolaReport(
        int copiedRows,
        int sourceRows,
        int targetRows,
        boolean reconciled) {
}
