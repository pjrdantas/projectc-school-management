package br.com.escola.bff.infra.webclient;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.PlanejamentoBimestralReadPort;
import br.com.escola.bff.infra.config.PlanejamentoIaServiceClientProperties;
import reactor.core.publisher.Mono;

@Component
public class PlanejamentoBimestralReadClient extends AbstractDownstreamClientSupport
        implements PlanejamentoBimestralReadPort {

    private final WebClient webClient;
    private final PlanejamentoIaServiceClientProperties properties;

    public PlanejamentoBimestralReadClient(
            @Qualifier("planningAiServiceWebClient") WebClient planningAiServiceWebClient,
            PlanejamentoIaServiceClientProperties properties) {
        this.webClient = planningAiServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> listar(
            UUID professorTurmaDisciplinaId,
            UUID periodoAvaliativoId,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.get()
                .uri(builder -> builder.path("/internal/v1/planejamentos-bimestrais")
                        .queryParamIfPresent(
                                "professorTurmaDisciplinaId",
                                Optional.ofNullable(professorTurmaDisciplinaId))
                        .queryParamIfPresent("periodoAvaliativoId", Optional.ofNullable(periodoAvaliativoId))
                        .build())
                .headers(headers -> preencherCabecalhos(headers, query, context))
                .exchangeToMono(response -> handle(response, "Planning AI service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Planning AI service indisponivel"));
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> buscarPorId(
            UUID planejamentoId,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.get()
                .uri("/internal/v1/planejamentos-bimestrais/{planejamentoId}", planejamentoId)
                .headers(headers -> preencherCabecalhos(headers, query, context))
                .exchangeToMono(response -> handle(response, "Planning AI service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Planning AI service indisponivel"));
    }

    private void preencherCabecalhos(
            org.springframework.http.HttpHeaders headers,
            CatalogReadQuery query,
            AuthSessionContext context) {
        headers.set("Authorization", query.authorization());
        headers.set("X-Internal-Token", properties.internalToken());
        headers.set("X-Correlation-Id", query.correlationId());
        headers.set("X-Usuario-Id", context.usuarioId().toString());
        headers.set("X-Escola-Id", context.escolaId().toString());
    }
}
