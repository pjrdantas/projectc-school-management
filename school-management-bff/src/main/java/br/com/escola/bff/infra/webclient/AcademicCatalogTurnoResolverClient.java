package br.com.escola.bff.infra.webclient;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.TurnoResolved;
import br.com.escola.bff.application.exception.DownstreamRejectedException;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.AcademicCatalogTurnoResolverPort;
import br.com.escola.bff.infra.config.CatalogServiceClientProperties;
import reactor.core.publisher.Mono;

@Component
public class AcademicCatalogTurnoResolverClient extends AbstractDownstreamClientSupport
        implements AcademicCatalogTurnoResolverPort {

    private final WebClient webClient;
    private final CatalogServiceClientProperties properties;

    public AcademicCatalogTurnoResolverClient(
            @Qualifier("catalogServiceWebClient")
            WebClient catalogServiceWebClient,
            CatalogServiceClientProperties properties) {
        this.webClient = catalogServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<TurnoResolved> resolve(CatalogWriteQuery query, AuthSessionContext context, String turno) {
        String codigoNormalizado = normalize(turno);
        return webClient.get()
                .uri("/internal/v1/turnos")
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
                .bodyToFlux(AcademicTurnoResponse.class)
                .collectList()
                .flatMap(responses -> resolve(responses, codigoNormalizado))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Academic catalog indisponivel"));
    }

    private Mono<TurnoResolved> resolve(List<AcademicTurnoResponse> responses, String codigoNormalizado) {
        return Mono.justOrEmpty(responses.stream()
                .filter(response -> codigoNormalizado.equals(normalize(response.codigo())))
                .findFirst()
                .map(response -> new TurnoResolved(response.id(), response.codigo())));
    }

    private String normalize(String value) {
        return value == null
                ? null
                : value.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
    }

    private record AcademicTurnoResponse(
            java.util.UUID id,
            String codigo,
            String descricao
    ) {}
}
