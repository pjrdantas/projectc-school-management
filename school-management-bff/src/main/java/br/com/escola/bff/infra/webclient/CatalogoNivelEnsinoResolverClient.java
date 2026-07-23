package br.com.escola.bff.infra.webclient;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.NivelEnsinoResolved;
import br.com.escola.bff.application.exception.DownstreamRejectedException;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.CatalogoNivelEnsinoResolverPort;
import br.com.escola.bff.infra.config.CatalogServiceClientProperties;
import reactor.core.publisher.Mono;

@Component
public class CatalogoNivelEnsinoResolverClient extends AbstractDownstreamClientSupport
        implements CatalogoNivelEnsinoResolverPort {

    private final WebClient webClient;
    private final CatalogServiceClientProperties properties;

    public CatalogoNivelEnsinoResolverClient(
            @Qualifier("catalogServiceWebClient")
            WebClient catalogServiceWebClient,
            CatalogServiceClientProperties properties) {
        this.webClient = catalogServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<NivelEnsinoResolved> resolve(CatalogWriteQuery query, AuthSessionContext context, String nivelEnsino) {
        String codigoNormalizado = normalize(nivelEnsino);
        return webClient.get()
                .uri("/internal/v1/catalogos/niveis-ensino")
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new DownstreamRejectedException(response.statusCode().value())))
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new DownstreamUnavailableException(
                                "Academic catalog retornou erro interno")))
                .bodyToFlux(AcademicNivelEnsinoResponse.class)
                .collectList()
                .flatMap(responses -> resolve(responses, codigoNormalizado))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Academic catalog indisponivel"));
    }

    private Mono<NivelEnsinoResolved> resolve(List<AcademicNivelEnsinoResponse> responses, String codigoNormalizado) {
        return Mono.justOrEmpty(responses.stream()
                .filter(response -> codigoNormalizado.equals(normalize(response.codigo())))
                .findFirst()
                .map(response -> new NivelEnsinoResolved(response.id(), response.codigo())));
    }

    private String normalize(String value) {
        return value == null
                ? null
                : value.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
    }

    private record AcademicNivelEnsinoResponse(
            java.util.UUID id,
            String codigo,
            String descricao
    ) {}
}

