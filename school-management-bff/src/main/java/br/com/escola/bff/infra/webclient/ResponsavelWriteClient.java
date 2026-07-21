package br.com.escola.bff.infra.webclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.ResponsavelWritePort;
import br.com.escola.bff.infra.config.ResponsavelCatalogoServiceClientProperties;
import reactor.core.publisher.Mono;

@Component
public class ResponsavelWriteClient extends AbstractDownstreamClientSupport implements ResponsavelWritePort {

    private final WebClient webClient;
    private final ResponsavelCatalogoServiceClientProperties properties;

    public ResponsavelWriteClient(
            @Qualifier("responsiblesServiceWebClient") WebClient responsiblesServiceWebClient,
            ResponsavelCatalogoServiceClientProperties properties) {
        this.webClient = responsiblesServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<ResponseEntity<String>> criar(String requestBody, CatalogReadQuery query, AuthSessionContext context) {
        return webClient.post()
                .uri("/internal/v1/responsaveis")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> addHeaders(headers, query, context))
                .bodyValue(requestBody)
                .exchangeToMono(response -> handle(response, "Responsibles service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Responsibles service indisponivel"));
    }

    @Override
    public Mono<ResponseEntity<String>> atualizar(
            UUID responsavelId,
            String requestBody,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.put()
                .uri("/internal/v1/responsaveis/{id}", responsavelId)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> addHeaders(headers, query, context))
                .bodyValue(requestBody)
                .exchangeToMono(response -> handle(response, "Responsibles service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Responsibles service indisponivel"));
    }

    @Override
    public Mono<ResponseEntity<String>> excluir(
            UUID responsavelId,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.delete()
                .uri("/internal/v1/responsaveis/{id}", responsavelId)
                .headers(headers -> addHeaders(headers, query, context))
                .exchangeToMono(response -> handle(response, "Responsibles service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Responsibles service indisponivel"));
    }

    private void addHeaders(
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
