package br.com.escola.bff.infra.webclient;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.infra.config.LegacyClientProperties;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import reactor.core.publisher.Mono;

@Component
public class LegacyAuthContextClient implements AuthContextPort {

    private final WebClient webClient;
    private final CircuitBreaker circuitBreaker;
    private final LegacyClientProperties properties;

    public LegacyAuthContextClient(
            @Qualifier("monolithWebClient")
            WebClient monolithWebClient,
            CircuitBreaker monolithCircuitBreaker,
            LegacyClientProperties properties) {
        this.webClient = monolithWebClient;
        this.circuitBreaker = monolithCircuitBreaker;
        this.properties = properties;
    }

    @Override
    public Mono<AuthSessionContext> resolve(CatalogReadQuery query) {
        return webClient.get()
                .uri("/api/auth/contexto-atual")
                .header("Authorization", query.authorization())
                .header(TrustedHeaders.CORRELATION_ID, query.correlationId())
                .retrieve()
                .onStatus(
                        org.springframework.http.HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new br.com.escola.bff.application.exception.DownstreamRejectedException(
                                response.statusCode().value())))
                .onStatus(
                        org.springframework.http.HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new br.com.escola.bff.application.exception.DownstreamUnavailableException(
                                "Monolito rejeitou a resolucao de contexto")))
                .bodyToMono(AuthContextResponse.class)
                .map(response -> new AuthSessionContext(response.usuarioId(), response.escolaId(), response.escolaNome()))
                .timeout(properties.responseTimeout())
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .onErrorMap(CallNotPermittedException.class,
                        error -> new br.com.escola.bff.application.exception.DownstreamUnavailableException(
                                "Circuit breaker do monolito esta aberto", error))
                .onErrorMap(error -> error instanceof java.util.concurrent.TimeoutException
                                || error instanceof org.springframework.web.reactive.function.client.WebClientRequestException,
                        error -> new br.com.escola.bff.application.exception.DownstreamUnavailableException(
                                "Monolito indisponivel", error));
    }

    private record AuthContextResponse(
            java.util.UUID usuarioId,
            java.util.UUID escolaId,
            String escolaNome
    ) {}
}

