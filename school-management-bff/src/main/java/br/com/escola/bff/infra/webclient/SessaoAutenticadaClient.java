package br.com.escola.bff.infra.webclient;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.SessaoAutenticadaPort;
import br.com.escola.bff.infra.config.AutenticacaoClientProperties;
import reactor.core.publisher.Mono;

@Component
public class SessaoAutenticadaClient extends AbstractDownstreamClientSupport implements SessaoAutenticadaPort {

    private final WebClient webClient;
    private final AutenticacaoClientProperties properties;

    public SessaoAutenticadaClient(
            @Qualifier("identityAccessServiceWebClient")
            WebClient identityAccessServiceWebClient,
            AutenticacaoClientProperties properties) {
        this.webClient = identityAccessServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<ResponseEntity<String>> listarEscolas(CatalogReadQuery query, AuthSessionContext context) {
        return webClient.get()
                .uri("/internal/v1/auth/escolas")
                .header(HttpHeaders.AUTHORIZATION, query.authorization())
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .exchangeToMono(response -> handle(response, "Identity access service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Identity access service indisponivel"));
    }

    @Override
    public Mono<ResponseEntity<String>> selecionarEscolaAtiva(
            String requestBody,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.post()
                .uri("/internal/v1/auth/escola-ativa")
                .header(HttpHeaders.AUTHORIZATION, query.authorization())
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchangeToMono(response -> handle(response, "Identity access service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Identity access service indisponivel"));
    }
}


