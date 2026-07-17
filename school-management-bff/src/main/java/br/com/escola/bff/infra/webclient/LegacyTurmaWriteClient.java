package br.com.escola.bff.infra.webclient;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.TurmaCreateCommand;
import br.com.escola.bff.application.dto.TurmaCreatedResult;
import br.com.escola.bff.application.exception.DownstreamRejectedException;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.LegacyTurmaWritePort;
import br.com.escola.bff.infra.config.LegacyClientProperties;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import reactor.core.publisher.Mono;

@Component
public class LegacyTurmaWriteClient implements LegacyTurmaWritePort {

    private final WebClient webClient;
    private final CircuitBreaker circuitBreaker;
    private final LegacyClientProperties properties;

    public LegacyTurmaWriteClient(
            @Qualifier("monolithWebClient")
            WebClient monolithWebClient,
            CircuitBreaker monolithCircuitBreaker,
            LegacyClientProperties properties) {
        this.webClient = monolithWebClient;
        this.circuitBreaker = monolithCircuitBreaker;
        this.properties = properties;
    }

    @Override
    public Mono<TurmaCreatedResult> criar(CatalogWriteQuery query, TurmaCreateCommand command) {
        return webClient.post()
                .uri("/api/turmas")
                .header("Authorization", query.authorization())
                .header(TrustedHeaders.CORRELATION_ID, query.correlationId())
                .bodyValue(command)
                .retrieve()
                .onStatus(
                        org.springframework.http.HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new DownstreamRejectedException(response.statusCode().value())))
                .onStatus(
                        org.springframework.http.HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new DownstreamUnavailableException("Monolito retornou erro interno")))
                .bodyToMono(TurmaCreatedResult.class)
                .timeout(properties.responseTimeout())
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .onErrorMap(CallNotPermittedException.class,
                        error -> new DownstreamUnavailableException("Circuit breaker do monolito esta aberto", error))
                .onErrorMap(error -> error instanceof java.util.concurrent.TimeoutException
                                || error instanceof org.springframework.web.reactive.function.client.WebClientRequestException,
                        error -> new DownstreamUnavailableException("Monolito indisponivel", error));
    }
}

