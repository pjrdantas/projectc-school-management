package br.com.escola.bff.application.port.out;

import br.com.escola.bff.application.service.CatalogReadCutoverDecision;

public interface CatalogReadObservabilityPort {

    void recordCatalogSuccess(CatalogReadCutoverDecision decision);

    void recordCatalogFailure(CatalogReadCutoverDecision decision, Throwable error);
}

