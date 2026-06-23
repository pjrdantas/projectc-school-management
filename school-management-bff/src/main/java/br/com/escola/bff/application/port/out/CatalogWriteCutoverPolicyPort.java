package br.com.escola.bff.application.port.out;

import br.com.escola.bff.application.service.CatalogWriteCutoverDecision;
import br.com.escola.bff.application.service.CatalogWriteRoute;

public interface CatalogWriteCutoverPolicyPort {

    CatalogWriteCutoverDecision decision(CatalogWriteRoute route);
}
