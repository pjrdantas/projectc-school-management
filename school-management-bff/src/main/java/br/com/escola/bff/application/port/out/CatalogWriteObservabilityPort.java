package br.com.escola.bff.application.port.out;

import br.com.escola.bff.application.service.CatalogWriteCutoverDecision;

public interface CatalogWriteObservabilityPort {

    void recordDirectLegacy(CatalogWriteCutoverDecision decision);

    void recordCatalogSuccess(CatalogWriteCutoverDecision decision);

    void recordCatalogFailure(CatalogWriteCutoverDecision decision, Throwable error);
}

