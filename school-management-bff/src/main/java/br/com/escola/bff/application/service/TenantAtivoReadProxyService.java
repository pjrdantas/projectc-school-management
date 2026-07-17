package br.com.escola.bff.application.service;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.IdentityTenantCutoverPolicyPort;
import br.com.escola.bff.application.port.out.IdentityTenantObservabilityPort;
import br.com.escola.bff.application.port.out.IdentityTenantAuthContextPort;
import br.com.escola.bff.application.port.out.TenantAtivoReadPort;
import br.com.escola.bff.application.port.out.LegacyTenantReadPort;
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
                        .onErrorMap(
                                DownstreamUnavailableException.class,
                                TenantAtivoReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> fallbackTenantAtivoParaMonolito(decision, query, "identity_access", error))
                .onErrorResume(TenantAtivoReadFailureException.class,
                        error -> fallbackTenantAtivoParaMonolito(
                                decision,
                                query,
                                "institutional_tenant",
                                error.cause()));
    }

    private Mono<ResponseEntity<String>> fallbackTenantAtivoParaMonolito(
            IdentityTenantCutoverDecision decision,
            CatalogReadQuery query,
            String target,
            DownstreamUnavailableException error) {
        observabilityPort.recordServiceFailure(decision, target, error);
        return cutoverPolicyPort.fallbackToLegacyOnError()
                ? monolithTenantReadPort.consultarTenantAtivo(query)
                        .doOnSuccess(response -> observabilityPort.recordFallbackToLegacy(
                                decision,
                                target,
                                error))
                : Mono.error(error);
    }

    private static final class TenantAtivoReadFailureException extends RuntimeException {

        private TenantAtivoReadFailureException(DownstreamUnavailableException cause) {
            super(cause);
        }

        private DownstreamUnavailableException cause() {
            return (DownstreamUnavailableException) getCause();
        }
    }
}

