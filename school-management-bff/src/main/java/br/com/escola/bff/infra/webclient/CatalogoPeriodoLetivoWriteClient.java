package br.com.escola.bff.infra.webclient;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.PeriodoLetivoCreateCommand;
import br.com.escola.bff.application.dto.PeriodoLetivoCreatedResult;
import br.com.escola.bff.application.port.out.CatalogoPeriodoLetivoWritePort;
import br.com.escola.bff.infra.config.CatalogServiceClientProperties;
import reactor.core.publisher.Mono;

@Component
public class CatalogoPeriodoLetivoWriteClient extends AbstractDownstreamClientSupport
        implements CatalogoPeriodoLetivoWritePort {

    private final WebClient webClient;
    private final CatalogServiceClientProperties properties;

    public CatalogoPeriodoLetivoWriteClient(
            @Qualifier("catalogServiceWebClient")
            WebClient catalogServiceWebClient,
            CatalogServiceClientProperties properties) {
        this.webClient = catalogServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<PeriodoLetivoCreatedResult> criar(
            CatalogWriteQuery query,
            AuthSessionContext context,
            PeriodoLetivoCreateCommand command) {
        return webClient.post()
                .uri("/internal/v1/periodos-letivos")
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .header("Idempotency-Key", query.idempotencyKey())
                .bodyValue(new AcademicCreatePeriodoLetivoRequest(
                        command.nome(),
                        resolveAno(command),
                        command.dataInicio(),
                        command.dataFim()))
                .exchangeToMono(response -> response.statusCode().is4xxClientError()
                        ? Mono.error(new br.com.escola.bff.application.exception.DownstreamRejectedException(
                                response.statusCode().value()))
                        : response.statusCode().is5xxServerError()
                                ? Mono.error(new br.com.escola.bff.application.exception.DownstreamUnavailableException(
                                        "Academic catalog retornou erro interno"))
                                : response.bodyToMono(AcademicPeriodoLetivoResponse.class)
                                        .map(body -> new PeriodoLetivoCreatedResult(
                                                body.id(),
                                                body.nome(),
                                                body.ano(),
                                                body.dataInicio(),
                                                body.dataFim(),
                                                body.ativo(),
                                                body.escolaId(),
                                                context.escolaNome(),
                                                body.createdAt())))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Academic catalog indisponivel"));
    }

    private int resolveAno(PeriodoLetivoCreateCommand command) {
        return command.ano() != null ? command.ano() : command.dataInicio().getYear();
    }

    private record AcademicCreatePeriodoLetivoRequest(
            String nome,
            int ano,
            java.time.LocalDate dataInicio,
            java.time.LocalDate dataFim
    ) {}

    private record AcademicPeriodoLetivoResponse(
            java.util.UUID id,
            String nome,
            int ano,
            java.time.LocalDate dataInicio,
            java.time.LocalDate dataFim,
            boolean ativo,
            java.util.UUID escolaId,
            java.time.LocalDateTime createdAt
    ) {}
}

