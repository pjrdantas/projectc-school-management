package br.com.escola.bff.application.service;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.CatalogReadObservabilityPort;
import br.com.escola.bff.application.port.out.CatalogoReadPort;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.usecase.RouteCatalogReadUseCase;
import reactor.core.publisher.Mono;

public class CatalogReadRoutingService implements RouteCatalogReadUseCase {

    private final CatalogoReadPort academicCatalogReadPort;
    private final InternalAuthContextPort authContextPort;
    private final CatalogReadObservabilityPort observabilityPort;

    public CatalogReadRoutingService(
            CatalogoReadPort academicCatalogReadPort,
            InternalAuthContextPort authContextPort,
            CatalogReadObservabilityPort observabilityPort) {
        this.academicCatalogReadPort = academicCatalogReadPort;
        this.authContextPort = authContextPort;
        this.observabilityPort = observabilityPort;
    }

    @Override
    public Mono<ResponseEntity<String>> executar(CatalogReadRoute route, CatalogReadQuery query, String... pathArgs) {
        CatalogReadCutoverDecision decision = new CatalogReadCutoverDecision(route, true, "catalog_official");
        String internalPath = route.internalPath(pathArgs);
        return authContextPort.resolve(query)
                .flatMap(context -> academicCatalogReadPort.get(internalPath, query, context)
                        .doOnSuccess(response -> observabilityPort.recordCatalogSuccess(decision))
                        .doOnError(error -> observabilityPort.recordCatalogFailure(decision, error)));
    }
}
