package br.com.escola.bff.application.service;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.InstitutionalTenantReadPort;
import br.com.escola.bff.application.usecase.ConsultarTenantAtivoUseCase;
import reactor.core.publisher.Mono;

public class InstitutionalTenantReadProxyService implements ConsultarTenantAtivoUseCase {

    private final AuthContextPort authContextPort;
    private final InstitutionalTenantReadPort institutionalTenantReadPort;

    public InstitutionalTenantReadProxyService(
            AuthContextPort authContextPort,
            InstitutionalTenantReadPort institutionalTenantReadPort) {
        this.authContextPort = authContextPort;
        this.institutionalTenantReadPort = institutionalTenantReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> consultarTenantAtivo(String authorization, String correlationId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> institutionalTenantReadPort.consultarTenantAtivo(query, context));
    }
}
