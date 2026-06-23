package br.com.escola.bff.infra.webclient;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.SerieCreateCommand;
import br.com.escola.bff.application.dto.SerieCreatedResult;
import br.com.escola.bff.application.exception.DownstreamRejectedException;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.MonolithSerieWritePort;
import br.com.escola.bff.infra.config.MonolithClientProperties;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import reactor.core.publisher.Mono;

@Component
public class MonolithSerieWriteClient implements MonolithSerieWritePort {

    private final WebClient webClient;
    private final CircuitBreaker circuitBreaker;
    private final MonolithClientProperties properties;

    public MonolithSerieWriteClient(
            @Qualifier("monolithWebClient")
            WebClient monolithWebClient,
            CircuitBreaker monolithCircuitBreaker,
            MonolithClientProperties properties) {
        this.webClient = monolithWebClient;
        this.circuitBreaker = monolithCircuitBreaker;
        this.properties = properties;
    }

    @Override
    public Mono<SerieCreatedResult> criar(CatalogWriteQuery query, SerieCreateCommand command) {
        return webClient.post()
                .uri("/api/series")
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
                .bodyToMono(SerieCreatedResult.class)
                .timeout(properties.responseTimeout())
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .onErrorMap(CallNotPermittedException.class,
                        error -> new DownstreamUnavailableException("Circuit breaker do monolito esta aberto", error))
                .onErrorMap(error -> error instanceof java.util.concurrent.TimeoutException
                                || error instanceof org.springframework.web.reactive.function.client.WebClientRequestException,
                        error -> new DownstreamUnavailableException("Monolito indisponivel", error));
    }
}
