package br.com.escola.bff.infra.webclient;

import java.util.List;
import java.util.concurrent.TimeoutException;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.dto.DisciplinaQuery;
import br.com.escola.bff.application.dto.DisciplinaView;
import br.com.escola.bff.application.exception.DownstreamRejectedException;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.DisciplinaQueryPort;
import br.com.escola.bff.infra.config.MonolithClientProperties;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import reactor.core.publisher.Mono;

@Component
public class MonolithDisciplinaClient implements DisciplinaQueryPort {

    private static final ParameterizedTypeReference<List<DisciplinaView>> RESPONSE_TYPE =
            new ParameterizedTypeReference<>() { };

    private final WebClient webClient;
    private final CircuitBreaker circuitBreaker;
    private final MonolithClientProperties properties;

    public MonolithDisciplinaClient(
            WebClient monolithWebClient,
            CircuitBreaker monolithCircuitBreaker,
            MonolithClientProperties properties) {
        this.webClient = monolithWebClient;
        this.circuitBreaker = monolithCircuitBreaker;
        this.properties = properties;
    }

    @Override
    public Mono<List<DisciplinaView>> listar(DisciplinaQuery query) {
        return webClient.get()
                .uri("/api/disciplinas")
                .header("Authorization", query.authorization())
                .header(TrustedHeaders.CORRELATION_ID, query.correlationId())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new DownstreamRejectedException(response.statusCode().value())))
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new DownstreamUnavailableException("Monolito retornou erro interno")))
                .bodyToMono(RESPONSE_TYPE)
                .timeout(properties.responseTimeout())
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .onErrorMap(CallNotPermittedException.class,
                        error -> new DownstreamUnavailableException("Circuit breaker do monolito esta aberto", error))
                .onErrorMap(
                        error -> error instanceof TimeoutException || error instanceof WebClientRequestException,
                        error -> new DownstreamUnavailableException("Monolito indisponivel", error));
    }
}
