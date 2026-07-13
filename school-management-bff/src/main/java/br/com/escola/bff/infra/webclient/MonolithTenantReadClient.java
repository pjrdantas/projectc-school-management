package br.com.escola.bff.infra.webclient;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.MonolithTenantReadPort;
import reactor.core.publisher.Mono;

@Component
public class MonolithTenantReadClient implements MonolithTenantReadPort {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public MonolithTenantReadClient(
            @Qualifier("monolithWebClient") WebClient monolithWebClient,
            ObjectMapper objectMapper) {
        this.webClient = monolithWebClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<ResponseEntity<String>> consultarTenantAtivo(CatalogReadQuery query) {
        return webClient.get()
                .uri("/api/auth/contexto-atual")
                .header(HttpHeaders.AUTHORIZATION, query.authorization())
                .header("X-Correlation-Id", query.correlationId())
                .retrieve()
                .onStatus(
                        org.springframework.http.HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new br.com.escola.bff.application.exception.DownstreamRejectedException(
                                response.statusCode().value())))
                .onStatus(
                        org.springframework.http.HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new DownstreamUnavailableException(
                                "Monolito rejeitou a resolucao de tenant ativo")))
                .bodyToMono(AuthContextResponse.class)
                .map(response -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(serialize(new TenantAtivoResponse(response.escolaId(), response.escolaNome()))))
                .onErrorMap(error -> error instanceof java.util.concurrent.TimeoutException
                                || error instanceof org.springframework.web.reactive.function.client.WebClientRequestException,
                        error -> new DownstreamUnavailableException("Monolito indisponivel", error));
    }

    private String serialize(TenantAtivoResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Nao foi possivel serializar o tenant ativo do monolito", exception);
        }
    }

    private record AuthContextResponse(
            java.util.UUID usuarioId,
            java.util.UUID escolaId,
            String escolaNome
    ) {}

    private record TenantAtivoResponse(
            java.util.UUID escolaId,
            String escolaNome
    ) {}
}
