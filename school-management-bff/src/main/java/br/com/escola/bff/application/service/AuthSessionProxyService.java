package br.com.escola.bff.application.service;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.IdentityTenantAuthContextPort;
import br.com.escola.bff.application.port.out.IdentityTenantObservabilityPort;
import br.com.escola.bff.application.port.out.SessaoAutenticadaPort;
import br.com.escola.bff.application.port.out.TenantAtivoReadPort;
import br.com.escola.bff.application.usecase.ConsultarAuthSessionUseCase;
import br.com.escola.bff.application.usecase.SelecionarEscolaAtivaUseCase;
import reactor.core.publisher.Mono;

public class AuthSessionProxyService implements ConsultarAuthSessionUseCase, SelecionarEscolaAtivaUseCase {

    private final IdentityTenantAuthContextPort authContextPort;
    private final SessaoAutenticadaPort identityAccessSessionPort;
    private final TenantAtivoReadPort institutionalTenantReadPort;
    private final IdentityTenantObservabilityPort observabilityPort;

    public AuthSessionProxyService(
            IdentityTenantAuthContextPort authContextPort,
            SessaoAutenticadaPort identityAccessSessionPort,
            TenantAtivoReadPort institutionalTenantReadPort,
            IdentityTenantObservabilityPort observabilityPort) {
        this.authContextPort = authContextPort;
        this.identityAccessSessionPort = identityAccessSessionPort;
        this.institutionalTenantReadPort = institutionalTenantReadPort;
        this.observabilityPort = observabilityPort;
    }

    @Override
    public Mono<ResponseEntity<String>> listarEscolas(String authorization, String correlationId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> institutionalTenantReadPort.listarEscolasDisponiveis(query, context)
                        .doOnSuccess(response -> observabilityPort.recordServiceSuccess(
                                new IdentityTenantCutoverDecision(
                                        IdentityTenantRoute.AUTH_ESCOLAS,
                                        true,
                                        "identity_access_official"),
                                "institutional_tenant"))
                        .doOnError(error -> observabilityPort.recordServiceFailure(
                                new IdentityTenantCutoverDecision(
                                        IdentityTenantRoute.AUTH_ESCOLAS,
                                        true,
                                        "identity_access_official"),
                                "institutional_tenant",
                                error)));
    }

    @Override
    public Mono<ResponseEntity<String>> selecionarEscolaAtiva(
            String authorization,
            String correlationId,
            String requestBody) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> identityAccessSessionPort.selecionarEscolaAtiva(requestBody, query, context)
                        .doOnSuccess(response -> observabilityPort.recordServiceSuccess(
                                new IdentityTenantCutoverDecision(
                                        IdentityTenantRoute.AUTH_ESCOLA_ATIVA,
                                        true,
                                        "identity_access_official"),
                                "identity_access"))
                        .doOnError(error -> observabilityPort.recordServiceFailure(
                                new IdentityTenantCutoverDecision(
                                        IdentityTenantRoute.AUTH_ESCOLA_ATIVA,
                                        true,
                                        "identity_access_official"),
                                "identity_access",
                                error)));
    }
}
