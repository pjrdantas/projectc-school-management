package br.com.escola.bff.infra.webclient;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.IdentityTenantAuthContextPort;
import br.com.escola.bff.infra.config.IdentityAccessServiceClientProperties;
import reactor.core.publisher.Mono;

@Component
public class IdentityAccessAuthContextClient implements IdentityTenantAuthContextPort, InternalAuthContextPort {

    private final WebClient webClient;
    private final IdentityAccessServiceClientProperties properties;

    public IdentityAccessAuthContextClient(
            @Qualifier("identityAccessServiceWebClient")
            WebClient identityAccessServiceWebClient,
            IdentityAccessServiceClientProperties properties) {
        this.webClient = identityAccessServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<AuthSessionContext> resolve(CatalogReadQuery query) {
        return webClient.get()
                .uri("/internal/v1/auth/contexto-atual")
                .header(HttpHeaders.AUTHORIZATION, query.authorization())
                .header("X-Internal-Token", properties.internalToken())
                .header(TrustedHeaders.CORRELATION_ID, query.correlationId())
                .exchangeToMono(response -> {
                    if (response.statusCode().is4xxClientError()) {
                        return Mono.error(new br.com.escola.bff.application.exception.DownstreamRejectedException(
                                response.statusCode().value()));
                    }
                    if (response.statusCode().is5xxServerError()) {
                        return Mono.error(new br.com.escola.bff.application.exception.DownstreamUnavailableException(
                                "Identity access service rejeitou a resolucao de contexto"));
                    }
                    return response.bodyToMono(AuthContextResponse.class)
                            .map(payload -> new AuthSessionContext(
                                    payload.usuarioId(),
                                    payload.escolaId(),
                                    payload.escolaNome()));
                })
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> error instanceof java.util.concurrent.TimeoutException
                                || error instanceof org.springframework.web.reactive.function.client.WebClientRequestException,
                        error -> new br.com.escola.bff.application.exception.DownstreamUnavailableException(
                                "Identity access service indisponivel", error));
    }

    private record AuthContextResponse(
            java.util.UUID usuarioId,
            java.util.UUID escolaId,
            String escolaNome
    ) {}
}
