package br.com.escola.bff.infra.webclient;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.TurmaCreateCommand;
import br.com.escola.bff.application.dto.TurmaCreatedResult;
import br.com.escola.bff.application.dto.TurnoResolved;
import br.com.escola.bff.application.port.out.AcademicCatalogTurmaWritePort;
import br.com.escola.bff.infra.config.CatalogServiceClientProperties;
import reactor.core.publisher.Mono;

@Component
public class AcademicCatalogTurmaWriteClient extends AbstractDownstreamClientSupport
        implements AcademicCatalogTurmaWritePort {

    private final WebClient webClient;
    private final CatalogServiceClientProperties properties;

    public AcademicCatalogTurmaWriteClient(
            @Qualifier("catalogServiceWebClient")
            WebClient catalogServiceWebClient,
            CatalogServiceClientProperties properties) {
        this.webClient = catalogServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<TurmaCreatedResult> criar(
            CatalogWriteQuery query,
            AuthSessionContext context,
            TurnoResolved turno,
            TurmaCreateCommand command) {
        return webClient.post()
                .uri("/internal/v1/turmas")
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .header("Idempotency-Key", query.idempotencyKey())
                .bodyValue(new AcademicCreateTurmaRequest(
                        command.codigo(),
                        command.nome(),
                        command.capacidade(),
                        command.periodoLetivoId(),
                        command.serieId(),
                        turno.id()))
                .exchangeToMono(response -> response.statusCode().is4xxClientError()
                        ? Mono.error(new br.com.escola.bff.application.exception.DownstreamRejectedException(
                                response.statusCode().value()))
                        : response.statusCode().is5xxServerError()
                                ? Mono.error(new br.com.escola.bff.application.exception.DownstreamUnavailableException(
                                        "Academic catalog retornou erro interno"))
                                : response.bodyToMono(AcademicTurmaResponse.class)
                                        .map(body -> new TurmaCreatedResult(
                                                body.id(),
                                                body.codigo(),
                                                body.nome(),
                                                body.capacidade(),
                                                body.periodoLetivoId(),
                                                body.serieId(),
                                                body.serieNome(),
                                                body.turnoCodigo(),
                                                body.ativo() ? "ATIVA" : "INATIVA",
                                                body.escolaId(),
                                                context.escolaNome(),
                                                body.createdAt())))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Academic catalog indisponivel"));
    }

    private record AcademicCreateTurmaRequest(
            String codigo,
            String nome,
            Integer capacidade,
            java.util.UUID periodoLetivoId,
            java.util.UUID serieId,
            java.util.UUID turnoId
    ) {}

    private record AcademicTurmaResponse(
            java.util.UUID id,
            String codigo,
            String nome,
            Integer capacidade,
            java.util.UUID periodoLetivoId,
            java.util.UUID serieId,
            String serieNome,
            java.util.UUID turnoId,
            String turnoCodigo,
            boolean ativo,
            java.util.UUID escolaId,
            java.time.LocalDateTime createdAt
    ) {}
}
