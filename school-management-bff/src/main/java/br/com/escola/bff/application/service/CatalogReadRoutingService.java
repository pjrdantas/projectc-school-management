package br.com.escola.bff.application.service;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.AcademicCatalogReadPort;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.CatalogReadCutoverPolicyPort;
import br.com.escola.bff.application.port.out.MonolithCatalogReadPort;
import br.com.escola.bff.application.usecase.RouteCatalogReadUseCase;
import reactor.core.publisher.Mono;

public class CatalogReadRoutingService implements RouteCatalogReadUseCase {

    private final MonolithCatalogReadPort monolithCatalogReadPort;
    private final AcademicCatalogReadPort academicCatalogReadPort;
    private final AuthContextPort authContextPort;
    private final CatalogReadCutoverPolicyPort cutoverPolicyPort;

    public CatalogReadRoutingService(
            MonolithCatalogReadPort monolithCatalogReadPort,
            AcademicCatalogReadPort academicCatalogReadPort,
            AuthContextPort authContextPort,
            CatalogReadCutoverPolicyPort cutoverPolicyPort) {
        this.monolithCatalogReadPort = monolithCatalogReadPort;
        this.academicCatalogReadPort = academicCatalogReadPort;
        this.authContextPort = authContextPort;
        this.cutoverPolicyPort = cutoverPolicyPort;
    }

    @Override
    public Mono<ResponseEntity<String>> executar(CatalogReadRoute route, CatalogReadQuery query, String... pathArgs) {
        String externalPath = route.externalPath(pathArgs);
        if (!cutoverPolicyPort.shouldUseCatalog(route)) {
            return monolithCatalogReadPort.get(externalPath, query);
        }

        String internalPath = route.internalPath(pathArgs);
        return authContextPort.resolve(query)
                .flatMap(context -> academicCatalogReadPort.get(internalPath, query, context))
                .onErrorResume(DownstreamUnavailableException.class, error ->
                        cutoverPolicyPort.fallbackToMonolithOnError()
                                ? monolithCatalogReadPort.get(externalPath, query)
                                : Mono.error(error));
    }
}
