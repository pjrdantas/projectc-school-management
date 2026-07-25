package br.com.escola.bff.infra.webclient;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import tools.jackson.databind.JsonNode;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.exception.DownstreamRejectedException;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.CatalogoMutationWritePort;
import br.com.escola.bff.infra.config.CatalogServiceClientProperties;
import reactor.core.publisher.Mono;

@Component
public class CatalogoMutationWriteClient extends AbstractDownstreamClientSupport implements CatalogoMutationWritePort {
    private final WebClient webClient;
    private final CatalogServiceClientProperties properties;

    public CatalogoMutationWriteClient(@Qualifier("catalogServiceWebClient") WebClient webClient,
            CatalogServiceClientProperties properties) {
        this.webClient = webClient;
        this.properties = properties;
    }

    @Override
    public Mono<MutationResponse> executar(HttpMethod method, String path, Object body,
            CatalogWriteQuery query, AuthSessionContext context) {
        WebClient.RequestBodySpec request = webClient.method(method).uri(path)
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .header("Idempotency-Key", query.idempotencyKey());
        return (body == null ? request : request.bodyValue(body)).exchangeToMono(response -> {
            if (response.statusCode().is4xxClientError()) return Mono.error(new DownstreamRejectedException(response.statusCode().value()));
            if (response.statusCode().is5xxServerError()) return Mono.error(new DownstreamUnavailableException("Academic catalog retornou erro interno"));
            boolean replayed = Boolean.parseBoolean(response.headers().header("Idempotency-Replayed").stream().findFirst().orElse("false"));
            return response.statusCode().is2xxSuccessful() && method == HttpMethod.DELETE
                    ? response.releaseBody().thenReturn(new MutationResponse(null, replayed))
                    : response.bodyToMono(JsonNode.class).map(value -> new MutationResponse(value, replayed));
        }).timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Academic catalog indisponivel"));
    }
}
