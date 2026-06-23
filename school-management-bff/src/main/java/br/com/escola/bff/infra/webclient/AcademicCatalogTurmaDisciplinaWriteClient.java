package br.com.escola.bff.infra.webclient;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.TurmaDisciplinaLinkCommand;
import br.com.escola.bff.application.dto.TurmaDisciplinaLinkedResult;
import br.com.escola.bff.application.port.out.AcademicCatalogTurmaDisciplinaWritePort;
import br.com.escola.bff.infra.config.CatalogServiceClientProperties;
import reactor.core.publisher.Mono;

@Component
public class AcademicCatalogTurmaDisciplinaWriteClient extends AbstractDownstreamClientSupport
        implements AcademicCatalogTurmaDisciplinaWritePort {

    private final WebClient webClient;
    private final CatalogServiceClientProperties properties;

    public AcademicCatalogTurmaDisciplinaWriteClient(
            @Qualifier("catalogServiceWebClient")
            WebClient catalogServiceWebClient,
            CatalogServiceClientProperties properties) {
        this.webClient = catalogServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<TurmaDisciplinaLinkedResult> vincular(
            CatalogWriteQuery query,
            AuthSessionContext context,
            TurmaDisciplinaLinkCommand command) {
        return webClient.post()
                .uri("/internal/v1/turmas/{turmaId}/disciplinas", command.turmaId())
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .header("Idempotency-Key", query.idempotencyKey())
                .bodyValue(new AcademicLinkTurmaDisciplinaRequest(command.disciplinaId(), command.cargaHoraria()))
                .exchangeToMono(response -> response.statusCode().is4xxClientError()
                        ? Mono.error(new br.com.escola.bff.application.exception.DownstreamRejectedException(
                                response.statusCode().value()))
                        : response.statusCode().is5xxServerError()
                                ? Mono.error(new br.com.escola.bff.application.exception.DownstreamUnavailableException(
                                        "Academic catalog retornou erro interno"))
                                : response.bodyToMono(TurmaDisciplinaLinkedResult.class))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Academic catalog indisponivel"));
    }

    private record AcademicLinkTurmaDisciplinaRequest(
            java.util.UUID disciplinaId,
            Integer cargaHoraria
    ) {}
}
