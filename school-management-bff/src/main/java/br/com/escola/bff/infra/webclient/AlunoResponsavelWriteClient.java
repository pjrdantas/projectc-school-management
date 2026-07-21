package br.com.escola.bff.infra.webclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.AlunoResponsavelWritePort;
import br.com.escola.bff.infra.config.ResponsavelCatalogoServiceClientProperties;
import reactor.core.publisher.Mono;

@Component
public class AlunoResponsavelWriteClient extends AbstractDownstreamClientSupport implements AlunoResponsavelWritePort {

    private final WebClient webClient;
    private final ResponsavelCatalogoServiceClientProperties properties;
    private final ObjectMapper objectMapper;

    public AlunoResponsavelWriteClient(
            @Qualifier("responsiblesServiceWebClient") WebClient responsiblesServiceWebClient,
            ResponsavelCatalogoServiceClientProperties properties,
            ObjectMapper objectMapper) {
        this.webClient = responsiblesServiceWebClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<ResponseEntity<String>> vincular(
            UUID alunoId,
            String requestBody,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.post()
                .uri("/internal/v1/alunos/{alunoId}/responsaveis", alunoId)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> addHeaders(headers, query, context))
                .bodyValue(toInternalRequest(requestBody))
                .exchangeToMono(response -> handle(response, "Responsibles service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Responsibles service indisponivel"));
    }

    @Override
    public Mono<ResponseEntity<String>> desvincular(
            UUID alunoId,
            UUID responsavelId,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.delete()
                .uri("/internal/v1/alunos/{alunoId}/responsaveis/{responsavelId}", alunoId, responsavelId)
                .headers(headers -> addHeaders(headers, query, context))
                .exchangeToMono(response -> handle(response, "Responsibles service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Responsibles service indisponivel"));
    }

    private String toInternalRequest(String requestBody) {
        try {
            JsonNode body = objectMapper.readTree(requestBody);
            if (!(body instanceof ObjectNode object)) {
                throw new IllegalArgumentException("Payload de vinculo deve ser um objeto JSON");
            }
            JsonNode responsavelId = object.remove("idResponsavel");
            if (responsavelId != null) {
                object.set("responsavelId", responsavelId);
            }
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Payload de vinculo invalido", exception);
        }
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
