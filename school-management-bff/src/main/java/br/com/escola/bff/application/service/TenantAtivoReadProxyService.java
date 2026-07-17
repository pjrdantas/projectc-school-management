package br.com.escola.bff.application.service;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.IdentityTenantAuthContextPort;
import br.com.escola.bff.application.port.out.IdentityTenantCutoverPolicyPort;
import br.com.escola.bff.application.port.out.IdentityTenantObservabilityPort;
import br.com.escola.bff.application.port.out.LegacyTenantReadPort;
import br.com.escola.bff.application.port.out.TenantAtivoReadPort;
import br.com.escola.bff.application.usecase.ConsultarTenantAtivoUseCase;
import reactor.core.publisher.Mono;

public class TenantAtivoReadProxyService implements ConsultarTenantAtivoUseCase {

    private final IdentityTenantAuthContextPort authContextPort;
    private final TenantAtivoReadPort institutionalTenantReadPort;
    private final LegacyTenantReadPort monolithTenantReadPort;
    private final IdentityTenantCutoverPolicyPort cutoverPolicyPort;
    private final IdentityTenantObservabilityPort observabilityPort;

    public TenantAtivoReadProxyService(
            IdentityTenantAuthContextPort authContextPort,
            TenantAtivoReadPort institutionalTenantReadPort,
            LegacyTenantReadPort monolithTenantReadPort,
            IdentityTenantCutoverPolicyPort cutoverPolicyPort,
            IdentityTenantObservabilityPort observabilityPort) {
        this.authContextPort = authContextPort;
        this.institutionalTenantReadPort = institutionalTenantReadPort;
        this.monolithTenantReadPort = monolithTenantReadPort;
        this.cutoverPolicyPort = cutoverPolicyPort;
        this.observabilityPort = observabilityPort;
    }

    @Override
    public Mono<ResponseEntity<String>> consultarTenantAtivo(String authorization, String correlationId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        IdentityTenantCutoverDecision decision = cutoverPolicyPort.decision(IdentityTenantRoute.AUTH_TENANT_ATIVA);
        if (!decision.useNewService()) {
            return monolithTenantReadPort.consultarTenantAtivo(query)
                    .doOnSuccess(response -> observabilityPort.recordDirectLegacy(decision));
        }
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
