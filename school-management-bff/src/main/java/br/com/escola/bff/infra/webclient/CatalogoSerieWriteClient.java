package br.com.escola.bff.infra.webclient;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.NivelEnsinoResolved;
import br.com.escola.bff.application.dto.SerieCreateCommand;
import br.com.escola.bff.application.dto.SerieCreatedResult;
import br.com.escola.bff.application.port.out.CatalogoSerieWritePort;
import br.com.escola.bff.infra.config.CatalogServiceClientProperties;
import reactor.core.publisher.Mono;

@Component
public class CatalogoSerieWriteClient extends AbstractDownstreamClientSupport
        implements CatalogoSerieWritePort {

    private final WebClient webClient;
    private final CatalogServiceClientProperties properties;

    public CatalogoSerieWriteClient(
            @Qualifier("catalogServiceWebClient")
            WebClient catalogServiceWebClient,
            CatalogServiceClientProperties properties) {
        this.webClient = catalogServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<SerieCreatedResult> criar(
            CatalogWriteQuery query,
            AuthSessionContext context,
            NivelEnsinoResolved nivelEnsino,
            SerieCreateCommand command) {
        return webClient.post()
                .uri("/internal/v1/series")
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .header("Idempotency-Key", query.idempotencyKey())
                .bodyValue(new AcademicCreateSerieRequest(command.nome(), command.ordem(), nivelEnsino.id()))
                .exchangeToMono(response -> response.statusCode().is4xxClientError()
                        ? Mono.error(new br.com.escola.bff.application.exception.DownstreamRejectedException(
                                response.statusCode().value()))
                        : response.statusCode().is5xxServerError()
                                ? Mono.error(new br.com.escola.bff.application.exception.DownstreamUnavailableException(
                                        "Academic catalog retornou erro interno"))
                                : response.bodyToMono(AcademicSerieResponse.class)
                                        .map(body -> new SerieCreatedResult(
                                                body.id(),
                                                body.nome(),
                                                body.ordem(),
                                                body.nivelEnsinoCodigo(),
                                                body.escolaId(),
                                                context.escolaNome(),
                                                body.createdAt())))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Academic catalog indisponivel"));
    }

    private record AcademicCreateSerieRequest(
            String nome,
            Integer ordem,
            java.util.UUID nivelEnsinoId
    ) {}

    private record AcademicSerieResponse(
            java.util.UUID id,
            String nome,
            Integer ordem,
            java.util.UUID nivelEnsinoId,
            String nivelEnsinoCodigo,
            java.util.UUID escolaId,
            java.time.LocalDateTime createdAt
    ) {}
}

