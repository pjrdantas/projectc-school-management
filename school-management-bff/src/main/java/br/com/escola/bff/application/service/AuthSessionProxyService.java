package br.com.escola.bff.application.service;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.IdentityTenantCutoverPolicyPort;
import br.com.escola.bff.application.port.out.IdentityTenantObservabilityPort;
import br.com.escola.bff.application.port.out.IdentityTenantAuthContextPort;
import br.com.escola.bff.application.port.out.IdentityAccessSessionPort;
import br.com.escola.bff.application.port.out.InstitutionalTenantReadPort;
import br.com.escola.bff.application.port.out.MonolithAuthSessionPort;
import br.com.escola.bff.application.usecase.ConsultarAuthSessionUseCase;
import br.com.escola.bff.application.usecase.SelecionarEscolaAtivaUseCase;
import reactor.core.publisher.Mono;

public class AuthSessionProxyService implements ConsultarAuthSessionUseCase, SelecionarEscolaAtivaUseCase {

    private final IdentityTenantAuthContextPort authContextPort;
    private final IdentityAccessSessionPort identityAccessSessionPort;
    private final InstitutionalTenantReadPort institutionalTenantReadPort;
    private final MonolithAuthSessionPort monolithAuthSessionPort;
    private final IdentityTenantCutoverPolicyPort cutoverPolicyPort;
    private final IdentityTenantObservabilityPort observabilityPort;

    public AuthSessionProxyService(
            IdentityTenantAuthContextPort authContextPort,
            IdentityAccessSessionPort identityAccessSessionPort,
            InstitutionalTenantReadPort institutionalTenantReadPort,
            MonolithAuthSessionPort monolithAuthSessionPort,
            IdentityTenantCutoverPolicyPort cutoverPolicyPort,
            IdentityTenantObservabilityPort observabilityPort) {
        this.authContextPort = authContextPort;
        this.identityAccessSessionPort = identityAccessSessionPort;
        this.institutionalTenantReadPort = institutionalTenantReadPort;
        this.monolithAuthSessionPort = monolithAuthSessionPort;
        this.cutoverPolicyPort = cutoverPolicyPort;
        this.observabilityPort = observabilityPort;
    }

    @Override
    public Mono<ResponseEntity<String>> listarEscolas(String authorization, String correlationId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        IdentityTenantCutoverDecision decision = cutoverPolicyPort.decision(IdentityTenantRoute.AUTH_ESCOLAS);
        if (!decision.useNewService()) {
            return monolithAuthSessionPort.listarEscolas(query)
                    .doOnSuccess(response -> observabilityPort.recordDirectMonolith(decision));
        }
        return authContextPort.resolve(query)
                .flatMap(context -> institutionalTenantReadPort.listarEscolasDisponiveis(query, context)
                        .doOnSuccess(response -> observabilityPort.recordServiceSuccess(
                                decision,
                                "institutional_tenant"))
                        .onErrorMap(
                                DownstreamUnavailableException.class,
                                InstitutionalTenantReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> fallbackListarEscolasParaMonolito(decision, query, "identity_access", error))
                .onErrorResume(InstitutionalTenantReadFailureException.class,
                        error -> fallbackListarEscolasParaMonolito(
                                decision,
                                query,
                                "institutional_tenant",
                                error.cause()));
    }

    @Override
    public Mono<ResponseEntity<String>> selecionarEscolaAtiva(
            String authorization,
            String correlationId,
            String requestBody) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        IdentityTenantCutoverDecision decision = cutoverPolicyPort.decision(IdentityTenantRoute.AUTH_ESCOLA_ATIVA);
        if (!decision.useNewService()) {
            return monolithAuthSessionPort.selecionarEscolaAtiva(requestBody, query)
                    .doOnSuccess(response -> observabilityPort.recordDirectMonolith(decision));
        }
        return authContextPort.resolve(query)
                .flatMap(context -> identityAccessSessionPort.selecionarEscolaAtiva(requestBody, query, context)
                        .doOnSuccess(response -> observabilityPort.recordServiceSuccess(decision, "identity_access")))
                .onErrorResume(DownstreamUnavailableException.class, error -> {
                    observabilityPort.recordServiceFailure(decision, "identity_access", error);
                    return cutoverPolicyPort.fallbackToMonolithOnError()
                            ? monolithAuthSessionPort.selecionarEscolaAtiva(requestBody, query)
                                    .doOnSuccess(response -> observabilityPort.recordFallbackToMonolith(
                                            decision,
                                            "identity_access",
                                            error))
                            : Mono.error(error);
                });
    }

    private Mono<ResponseEntity<String>> fallbackListarEscolasParaMonolito(
            IdentityTenantCutoverDecision decision,
            CatalogReadQuery query,
            String target,
            DownstreamUnavailableException error) {
        observabilityPort.recordServiceFailure(decision, target, error);
        return cutoverPolicyPort.fallbackToMonolithOnError()
                ? monolithAuthSessionPort.listarEscolas(query)
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
