package br.com.escola.bff.application.service;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.IdentityAccessSessionPort;
import br.com.escola.bff.application.usecase.ConsultarAuthSessionUseCase;
import br.com.escola.bff.application.usecase.SelecionarEscolaAtivaUseCase;
import reactor.core.publisher.Mono;

public class AuthSessionProxyService implements ConsultarAuthSessionUseCase, SelecionarEscolaAtivaUseCase {

    private final AuthContextPort authContextPort;
    private final IdentityAccessSessionPort identityAccessSessionPort;

    public AuthSessionProxyService(
            AuthContextPort authContextPort,
            IdentityAccessSessionPort identityAccessSessionPort) {
        this.authContextPort = authContextPort;
        this.identityAccessSessionPort = identityAccessSessionPort;
    }

    @Override
    public Mono<ResponseEntity<String>> listarEscolas(String authorization, String correlationId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> identityAccessSessionPort.listarEscolas(query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> selecionarEscolaAtiva(
            String authorization,
            String correlationId,
            String requestBody) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> identityAccessSessionPort.selecionarEscolaAtiva(requestBody, query, context));
    }
}
