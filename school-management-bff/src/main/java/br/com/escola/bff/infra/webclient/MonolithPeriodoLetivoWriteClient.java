package br.com.escola.bff.infra.webclient;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.PeriodoLetivoCreateCommand;
import br.com.escola.bff.application.dto.PeriodoLetivoCreatedResult;
import br.com.escola.bff.application.exception.DownstreamRejectedException;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.MonolithPeriodoLetivoWritePort;
import br.com.escola.bff.infra.config.MonolithClientProperties;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import reactor.core.publisher.Mono;

@Component
public class MonolithPeriodoLetivoWriteClient implements MonolithPeriodoLetivoWritePort {

    private final WebClient webClient;
    private final CircuitBreaker circuitBreaker;
    private final MonolithClientProperties properties;

    public MonolithPeriodoLetivoWriteClient(
            @Qualifier("monolithWebClient")
            WebClient monolithWebClient,
            CircuitBreaker monolithCircuitBreaker,
            MonolithClientProperties properties) {
        this.webClient = monolithWebClient;
        this.circuitBreaker = monolithCircuitBreaker;
        this.properties = properties;
    }

    @Override
    public Mono<PeriodoLetivoCreatedResult> criar(CatalogWriteQuery query, PeriodoLetivoCreateCommand command) {
        return webClient.post()
                .uri("/api/periodos-letivos")
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
                .bodyToMono(PeriodoLetivoCreatedResult.class)
                .timeout(properties.responseTimeout())
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .onErrorMap(CallNotPermittedException.class,
                        error -> new DownstreamUnavailableException("Circuit breaker do monolito esta aberto", error))
                .onErrorMap(error -> error instanceof java.util.concurrent.TimeoutException
                                || error instanceof org.springframework.web.reactive.function.client.WebClientRequestException,
                        error -> new DownstreamUnavailableException("Monolito indisponivel", error));
    }
}
