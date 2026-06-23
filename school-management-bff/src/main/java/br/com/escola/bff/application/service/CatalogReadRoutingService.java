package br.com.escola.bff.application.service;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.AcademicCatalogReadPort;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.CatalogReadCutoverPolicyPort;
import br.com.escola.bff.application.port.out.CatalogReadObservabilityPort;
import br.com.escola.bff.application.port.out.MonolithCatalogReadPort;
import br.com.escola.bff.application.usecase.RouteCatalogReadUseCase;
import reactor.core.publisher.Mono;

public class CatalogReadRoutingService implements RouteCatalogReadUseCase {

    private final MonolithCatalogReadPort monolithCatalogReadPort;
    private final AcademicCatalogReadPort academicCatalogReadPort;
    private final AuthContextPort authContextPort;
    private final CatalogReadCutoverPolicyPort cutoverPolicyPort;
    private final CatalogReadObservabilityPort observabilityPort;

    public CatalogReadRoutingService(
            MonolithCatalogReadPort monolithCatalogReadPort,
            AcademicCatalogReadPort academicCatalogReadPort,
            AuthContextPort authContextPort,
            CatalogReadCutoverPolicyPort cutoverPolicyPort,
            CatalogReadObservabilityPort observabilityPort) {
        this.monolithCatalogReadPort = monolithCatalogReadPort;
        this.academicCatalogReadPort = academicCatalogReadPort;
        this.authContextPort = authContextPort;
        this.cutoverPolicyPort = cutoverPolicyPort;
        this.observabilityPort = observabilityPort;
    }

    @Override
    public Mono<ResponseEntity<String>> executar(CatalogReadRoute route, CatalogReadQuery query, String... pathArgs) {
        String externalPath = route.externalPath(pathArgs);
        CatalogReadCutoverDecision decision = cutoverPolicyPort.decision(route);
        if (!decision.useCatalog()) {
            return monolithCatalogReadPort.get(externalPath, query)
                    .doOnSuccess(response -> observabilityPort.recordDirectMonolith(decision));
        }

        String internalPath = route.internalPath(pathArgs);
        return authContextPort.resolve(query)
                .flatMap(context -> academicCatalogReadPort.get(internalPath, query, context)
                        .doOnSuccess(response -> observabilityPort.recordCatalogSuccess(decision)))
                .onErrorResume(DownstreamUnavailableException.class, error -> {
                    observabilityPort.recordCatalogFailure(decision, error);
                    return cutoverPolicyPort.fallbackToMonolithOnError()
                            ? monolithCatalogReadPort.get(externalPath, query)
                                    .doOnSuccess(response -> observabilityPort.recordFallbackToMonolith(decision, error))
                            : Mono.error(error);
                });
    }
}
