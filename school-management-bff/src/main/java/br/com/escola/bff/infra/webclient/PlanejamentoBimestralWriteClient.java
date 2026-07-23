package br.com.escola.bff.infra.webclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.PlanejamentoBimestralWritePort;
import br.com.escola.bff.infra.config.PlanejamentoIaServiceClientProperties;
import reactor.core.publisher.Mono;

@Component
public class PlanejamentoBimestralWriteClient extends AbstractDownstreamClientSupport
        implements PlanejamentoBimestralWritePort {

    private final WebClient webClient;
    private final PlanejamentoIaServiceClientProperties properties;

    public PlanejamentoBimestralWriteClient(
            @Qualifier("planningAiServiceWebClient") WebClient planningAiServiceWebClient,
            PlanejamentoIaServiceClientProperties properties) {
        this.webClient = planningAiServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> criar(
            String requestBody,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return enviar(webClient.post().uri("/internal/v1/planejamentos-bimestrais"), requestBody, query, context);
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> atualizar(
            UUID planejamentoId,
            String requestBody,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return enviar(
                webClient.put().uri("/internal/v1/planejamentos-bimestrais/{planejamentoId}", planejamentoId),
                requestBody,
                query,
                context);
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> alterarStatus(
            UUID planejamentoId,
            String requestBody,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return enviar(
                webClient.patch().uri("/internal/v1/planejamentos-bimestrais/{planejamentoId}/status", planejamentoId),
                requestBody,
                query,
                context);
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> adicionarAula(
            UUID planejamentoId, String requestBody, CatalogReadQuery query, AuthSessionContext context) {
        return enviar(webClient.post().uri("/internal/v1/planejamentos-bimestrais/{planejamentoId}/aulas-previstas", planejamentoId),
                requestBody, query, context);
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> adicionarAvaliacao(
            UUID planejamentoId, String requestBody, CatalogReadQuery query, AuthSessionContext context) {
        return enviar(webClient.post().uri("/internal/v1/planejamentos-bimestrais/{planejamentoId}/avaliacoes-previstas", planejamentoId),
                requestBody, query, context);
    }

    private Mono<org.springframework.http.ResponseEntity<String>> enviar(
            WebClient.RequestBodySpec request,
            String requestBody,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return request.contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", query.authorization())
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .bodyValue(requestBody)
                .exchangeToMono(response -> handle(response, "Planning AI service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Planning AI service indisponivel"));
    }
}
