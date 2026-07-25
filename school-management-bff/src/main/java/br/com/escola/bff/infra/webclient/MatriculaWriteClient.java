package br.com.escola.bff.infra.webclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.MatriculaWritePort;
import br.com.escola.bff.infra.config.DocumentoMatriculaServiceClientProperties;
import reactor.core.publisher.Mono;

@Component
public class MatriculaWriteClient extends AbstractDownstreamClientSupport implements MatriculaWritePort {

    private final WebClient webClient;
    private final DocumentoMatriculaServiceClientProperties properties;
    private final ObjectMapper objectMapper;

    public MatriculaWriteClient(
            @Qualifier("enrollmentDocumentServiceWebClient") WebClient enrollmentDocumentServiceWebClient,
            DocumentoMatriculaServiceClientProperties properties,
            ObjectMapper objectMapper) {
        this.webClient = enrollmentDocumentServiceWebClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<ResponseEntity<String>> criar(String requestBody, CatalogReadQuery query, AuthSessionContext context) {
        return webClient.post()
                .uri("/internal/v1/matriculas")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> addHeaders(headers, query, context))
                .bodyValue(requestBody)
                .exchangeToMono(response -> handle(response, "Enrollment document service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Enrollment document service indisponivel"));
    }

    @Override
    public Mono<ResponseEntity<String>> atualizar(
            UUID matriculaId,
            String requestBody,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.put()
                .uri("/internal/v1/matriculas/{matriculaId}", matriculaId)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> addHeaders(headers, query, context))
                .bodyValue(requestBody)
                .exchangeToMono(response -> handle(response, "Enrollment document service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Enrollment document service indisponivel"));
    }

    @Override
    public Mono<ResponseEntity<String>> atualizarStatus(
            UUID matriculaId,
            String requestBody,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.patch()
                .uri("/internal/v1/matriculas/{matriculaId}/status", matriculaId)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> addHeaders(headers, query, context))
                .bodyValue(toStatusInternalRequest(requestBody))
                .exchangeToMono(response -> handle(response, "Enrollment document service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Enrollment document service indisponivel"));
    }

    @Override
    public Mono<ResponseEntity<String>> cancelar(
            UUID matriculaId,
            String requestBody,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.post()
                .uri("/internal/v1/matriculas/{matriculaId}/cancelamento", matriculaId)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> addHeaders(headers, query, context))
                .bodyValue(toCancelamentoInternalRequest(requestBody))
                .exchangeToMono(response -> handle(response, "Enrollment document service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Enrollment document service indisponivel"));
    }

    private String toStatusInternalRequest(String requestBody) {
        ObjectNode body = readObject(requestBody, "Payload de status invalido");
        JsonNode justificativa = body.remove("justificativa");
        if (justificativa != null) {
            body.set("observacao", justificativa);
        }
        return writeObject(body, "Payload de status invalido");
    }

    private String toCancelamentoInternalRequest(String requestBody) {
        if (requestBody == null || requestBody.isBlank()) {
            return "{\"motivo\":\"Cancelamento solicitado no BFF\"}";
        }
        ObjectNode body = readObject(requestBody, "Payload de cancelamento invalido");
        JsonNode justificativa = body.remove("justificativa");
        if (justificativa != null && body.get("motivo") == null) {
            body.set("motivo", justificativa);
        }
        return writeObject(body, "Payload de cancelamento invalido");
    }

    private ObjectNode readObject(String requestBody, String message) {
        try {
            JsonNode body = objectMapper.readTree(requestBody);
            if (body instanceof ObjectNode object) {
                return object;
            }
            throw new IllegalArgumentException(message);
        } catch (JacksonException exception) {
            throw new IllegalArgumentException(message, exception);
        }
    }

    private String writeObject(ObjectNode object, String message) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JacksonException exception) {
            throw new IllegalArgumentException(message, exception);
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
