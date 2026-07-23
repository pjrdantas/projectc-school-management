package br.com.escola.bff.infra.webclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.AulaPort;
import br.com.escola.bff.infra.config.EnsinoClientProperties;
import reactor.core.publisher.Mono;

@Component
public class AulaClient extends AbstractDownstreamClientSupport implements AulaPort {

    private final WebClient webClient;
    private final EnsinoClientProperties properties;

    public AulaClient(
            @Qualifier("pedagogicalServiceWebClient") WebClient pedagogicalServiceWebClient,
            EnsinoClientProperties properties) {
        this.webClient = pedagogicalServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> criar(
            String requestBody,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.post()
                .uri("/internal/v1/aulas")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", query.authorization())
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .bodyValue(requestBody)
                .exchangeToMono(response -> handle(response, "Pedagogical service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Pedagogical service indisponivel"));
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> listar(
            UUID professorTurmaDisciplinaId,
            UUID turmaId,
            CatalogReadQuery query,
            AuthSessionContext context) {
        String uri = UriComponentsBuilder.fromPath("/internal/v1/aulas")
                .queryParamIfPresent("professorTurmaDisciplinaId", java.util.Optional.ofNullable(professorTurmaDisciplinaId))
                .queryParamIfPresent("turmaId", java.util.Optional.ofNullable(turmaId))
                .build()
                .toUriString();
        return webClient.get()
                .uri(uri)
                .header("Authorization", query.authorization())
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .exchangeToMono(response -> handle(response, "Pedagogical service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Pedagogical service indisponivel"));
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> buscarPorId(
            UUID aulaId,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.get()
                .uri("/internal/v1/aulas/{id}", aulaId)
                .header("Authorization", query.authorization())
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .exchangeToMono(response -> handle(response, "Pedagogical service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Pedagogical service indisponivel"));
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> registrarFrequenciaProfessor(
            UUID aulaId,
            String requestBody,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.post()
                .uri("/internal/v1/aulas/{id}/frequencia-professor", aulaId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", query.authorization())
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .bodyValue(requestBody)
                .exchangeToMono(response -> handle(response, "Pedagogical service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Pedagogical service indisponivel"));
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> listarFrequenciaProfessor(
            UUID aulaId,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.get()
                .uri("/internal/v1/aulas/{id}/frequencia-professor", aulaId)
                .header("Authorization", query.authorization())
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .exchangeToMono(response -> handle(response, "Pedagogical service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Pedagogical service indisponivel"));
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> registrarFrequenciaAluno(
            UUID aulaId,
            String requestBody,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.post()
                .uri("/internal/v1/aulas/{id}/frequencias-alunos", aulaId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", query.authorization())
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .bodyValue(requestBody)
                .exchangeToMono(response -> handle(response, "Pedagogical service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Pedagogical service indisponivel"));
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> listarFrequenciasAlunos(
            UUID aulaId,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.get()
                .uri("/internal/v1/aulas/{id}/frequencias-alunos", aulaId)
                .header("Authorization", query.authorization())
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .exchangeToMono(response -> handle(response, "Pedagogical service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Pedagogical service indisponivel"));
    }
}


