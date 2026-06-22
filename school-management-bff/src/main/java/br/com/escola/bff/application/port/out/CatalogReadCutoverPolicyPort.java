package br.com.escola.bff.application.port.out;

import br.com.escola.bff.application.service.CatalogReadRoute;

public interface CatalogReadCutoverPolicyPort {

    boolean shouldUseCatalog(CatalogReadRoute route);

    boolean fallbackToMonolithOnError();
}
