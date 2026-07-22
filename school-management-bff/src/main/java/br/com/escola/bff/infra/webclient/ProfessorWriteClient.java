package br.com.escola.bff.infra.webclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.ProfessorWritePort;
import br.com.escola.bff.infra.config.ProfessorServiceClientProperties;
import reactor.core.publisher.Mono;

@Component
public class ProfessorWriteClient extends AbstractDownstreamClientSupport implements ProfessorWritePort {

    private final WebClient webClient;
    private final ProfessorServiceClientProperties properties;

    public ProfessorWriteClient(
            @Qualifier("academicProfessorServiceWebClient") WebClient academicProfessorServiceWebClient,
            ProfessorServiceClientProperties properties) {
        this.webClient = academicProfessorServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<ResponseEntity<String>> criarProfessor(
            String requestBody,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return post("/internal/v1/professores", requestBody, query, context);
    }

    @Override
    public Mono<ResponseEntity<String>> criarAlocacao(
            UUID professorId,
            String requestBody,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return post("/internal/v1/professores/" + professorId + "/turmas-disciplinas", requestBody, query, context);
    }

    @Override
    public Mono<ResponseEntity<String>> atualizarProfessor(
            UUID professorId,
            String requestBody,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.put()
                .uri("/internal/v1/professores/{professorId}", professorId)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> addHeaders(headers, query, context))
                .bodyValue(requestBody)
                .exchangeToMono(response -> handle(response, "Academic professor service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Academic professor service indisponivel"));
    }

    @Override
    public Mono<ResponseEntity<String>> atualizarAlocacao(
            UUID professorId,
            UUID alocacaoId,
            String requestBody,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.put()
                .uri("/internal/v1/professores/{professorId}/turmas-disciplinas/{alocacaoId}", professorId, alocacaoId)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> addHeaders(headers, query, context))
                .bodyValue(requestBody)
                .exchangeToMono(response -> handle(response, "Academic professor service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Academic professor service indisponivel"));
    }

    @Override
    public Mono<ResponseEntity<String>> encerrarAlocacao(
            UUID professorId,
            UUID alocacaoId,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.delete()
                .uri("/internal/v1/professores/{professorId}/turmas-disciplinas/{alocacaoId}", professorId, alocacaoId)
                .headers(headers -> addHeaders(headers, query, context))
                .exchangeToMono(response -> handle(response, "Academic professor service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Academic professor service indisponivel"));
    }

    private Mono<ResponseEntity<String>> post(
            String path,
            String requestBody,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> addHeaders(headers, query, context))
                .bodyValue(requestBody)
                .exchangeToMono(response -> handle(response, "Academic professor service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Academic professor service indisponivel"));
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
