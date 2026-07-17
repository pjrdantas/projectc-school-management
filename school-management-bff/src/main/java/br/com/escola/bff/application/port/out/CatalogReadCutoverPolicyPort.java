package br.com.escola.bff.application.port.out;

import br.com.escola.bff.application.service.CatalogReadRoute;
import br.com.escola.bff.application.service.CatalogReadCutoverDecision;

public interface CatalogReadCutoverPolicyPort {

    CatalogReadCutoverDecision decision(CatalogReadRoute route);

    boolean fallbackToLegacyOnError();
}

