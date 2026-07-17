package br.com.escola.bff.application.service;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.IdentityTenantAuthContextPort;
import br.com.escola.bff.application.port.out.IdentityTenantObservabilityPort;
import br.com.escola.bff.application.port.out.TenantAtivoReadPort;
import br.com.escola.bff.application.usecase.ConsultarTenantAtivoUseCase;
import reactor.core.publisher.Mono;

public class TenantAtivoReadProxyService implements ConsultarTenantAtivoUseCase {

    private final IdentityTenantAuthContextPort authContextPort;
    private final TenantAtivoReadPort institutionalTenantReadPort;
    private final IdentityTenantObservabilityPort observabilityPort;

    public TenantAtivoReadProxyService(
            IdentityTenantAuthContextPort authContextPort,
            TenantAtivoReadPort institutionalTenantReadPort,
            IdentityTenantObservabilityPort observabilityPort) {
        this.authContextPort = authContextPort;
        this.institutionalTenantReadPort = institutionalTenantReadPort;
        this.observabilityPort = observabilityPort;
    }

    @Override
    public Mono<ResponseEntity<String>> consultarTenantAtivo(String authorization, String correlationId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        IdentityTenantCutoverDecision decision = new IdentityTenantCutoverDecision(
                IdentityTenantRoute.AUTH_TENANT_ATIVA,
                true,
                "institutional_tenant_official");
        return authContextPort.resolve(query)
                .flatMap(context -> institutionalTenantReadPort.consultarTenantAtivo(query, context)
                        .doOnSuccess(response -> observabilityPort.recordServiceSuccess(
                                decision,
                                "institutional_tenant"))
                        .doOnError(error -> observabilityPort.recordServiceFailure(
                                decision,
                                "institutional_tenant",
                                error)));
    }
}
