package br.com.escola.bff.infra.webclient;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.DisciplinaCreateCommand;
import br.com.escola.bff.application.dto.DisciplinaCreatedResult;
import br.com.escola.bff.application.port.out.CatalogoDisciplinaWritePort;
import br.com.escola.bff.infra.config.CatalogServiceClientProperties;
import reactor.core.publisher.Mono;

@Component
public class CatalogoDisciplinaWriteClient extends AbstractDownstreamClientSupport
        implements CatalogoDisciplinaWritePort {

    private final WebClient webClient;
    private final CatalogServiceClientProperties properties;

    public CatalogoDisciplinaWriteClient(
            @Qualifier("catalogServiceWebClient")
            WebClient catalogServiceWebClient,
            CatalogServiceClientProperties properties) {
        this.webClient = catalogServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<DisciplinaCreatedResult> criar(
            CatalogWriteQuery query,
            AuthSessionContext context,
            DisciplinaCreateCommand command) {
        return webClient.post()
                .uri("/internal/v1/disciplinas")
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .header("Idempotency-Key", query.idempotencyKey())
                .bodyValue(new AcademicCreateDisciplinaRequest(
                        command.nome(),
                        command.cargaHoraria(),
                        toAtivo(command.status())))
                .exchangeToMono(response -> response.statusCode().is4xxClientError()
                        ? Mono.error(new br.com.escola.bff.application.exception.DownstreamRejectedException(
                                response.statusCode().value()))
                        : response.statusCode().is5xxServerError()
                                ? Mono.error(new br.com.escola.bff.application.exception.DownstreamUnavailableException(
                                        "Academic catalog retornou erro interno"))
                                : response.bodyToMono(AcademicDisciplinaResponse.class)
                                        .map(body -> new DisciplinaCreatedResult(
                                                body.id(),
                                                body.nome(),
                                                body.cargaHoraria(),
                                                body.ativo() ? "ATIVA" : "INATIVA",
                                                body.escolaId(),
                                                context.escolaNome(),
                                                body.createdAt())))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Academic catalog indisponivel"));
    }

    private Boolean toAtivo(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return !"INATIVA".equalsIgnoreCase(status.trim());
    }

    private record AcademicCreateDisciplinaRequest(
            String nome,
            Integer cargaHoraria,
            Boolean ativo
    ) {}

    private record AcademicDisciplinaResponse(
            java.util.UUID id,
            String nome,
            Integer cargaHoraria,
            boolean ativo,
            java.util.UUID escolaId,
            java.time.LocalDateTime createdAt
    ) {}
}

