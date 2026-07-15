package br.com.escola.bff.application.service;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.IdentityTenantCutoverPolicyPort;
import br.com.escola.bff.application.port.out.IdentityTenantObservabilityPort;
import br.com.escola.bff.application.port.out.IdentityTenantAuthContextPort;
import br.com.escola.bff.application.port.out.InstitutionalTenantReadPort;
import br.com.escola.bff.application.port.out.MonolithTenantReadPort;
import br.com.escola.bff.application.usecase.ConsultarTenantAtivoUseCase;
import reactor.core.publisher.Mono;

public class InstitutionalTenantReadProxyService implements ConsultarTenantAtivoUseCase {

    private final IdentityTenantAuthContextPort authContextPort;
    private final InstitutionalTenantReadPort institutionalTenantReadPort;
    private final MonolithTenantReadPort monolithTenantReadPort;
    private final IdentityTenantCutoverPolicyPort cutoverPolicyPort;
    private final IdentityTenantObservabilityPort observabilityPort;

    public InstitutionalTenantReadProxyService(
            IdentityTenantAuthContextPort authContextPort,
            InstitutionalTenantReadPort institutionalTenantReadPort,
            MonolithTenantReadPort monolithTenantReadPort,
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
                    .doOnSuccess(response -> observabilityPort.recordDirectMonolith(decision));
        }
        return authContextPort.resolve(query)
                .flatMap(context -> institutionalTenantReadPort.consultarTenantAtivo(query, context)
                        .doOnSuccess(response -> observabilityPort.recordServiceSuccess(
                                decision,
                                "institutional_tenant"))
                        .onErrorMap(
                                DownstreamUnavailableException.class,
                                InstitutionalTenantReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> fallbackTenantAtivoParaMonolito(decision, query, "identity_access", error))
                .onErrorResume(InstitutionalTenantReadFailureException.class,
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
        return cutoverPolicyPort.fallbackToMonolithOnError()
                ? monolithTenantReadPort.consultarTenantAtivo(query)
                        .doOnSuccess(response -> observabilityPort.recordFallbackToMonolith(
                                decision,
                                target,
                                error))
                : Mono.error(error);
    }

    private static final class InstitutionalTenantReadFailureException extends RuntimeException {

        private InstitutionalTenantReadFailureException(DownstreamUnavailableException cause) {
            super(cause);
        }

        private DownstreamUnavailableException cause() {
            return (DownstreamUnavailableException) getCause();
        }
    }
}
