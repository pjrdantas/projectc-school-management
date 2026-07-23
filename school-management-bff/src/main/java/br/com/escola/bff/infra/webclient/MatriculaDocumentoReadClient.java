package br.com.escola.bff.infra.webclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.MatriculaDocumentoReadPort;
import br.com.escola.bff.infra.config.DocumentoMatriculaServiceClientProperties;
import reactor.core.publisher.Mono;

@Component
public class MatriculaDocumentoReadClient extends AbstractDownstreamClientSupport
        implements MatriculaDocumentoReadPort {

    private final WebClient webClient;
    private final DocumentoMatriculaServiceClientProperties properties;

    public MatriculaDocumentoReadClient(
            @Qualifier("enrollmentDocumentServiceWebClient")
            WebClient enrollmentDocumentServiceWebClient,
            DocumentoMatriculaServiceClientProperties properties) {
        this.webClient = enrollmentDocumentServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> listarStatus(
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.get().uri("/internal/v1/matriculas/catalogos/status")
                .header("Authorization", query.authorization())
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .exchangeToMono(response -> handle(response, "Enrollment document service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Enrollment document service indisponivel"));
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> listarMatriculas(
            UUID alunoId,
            UUID turmaId,
            UUID periodoLetivoId,
            String status,
            CatalogReadQuery query,
            AuthSessionContext context) {
        String uri = UriComponentsBuilder.fromPath("/internal/v1/matriculas")
                .queryParamIfPresent("alunoId", java.util.Optional.ofNullable(alunoId))
                .queryParamIfPresent("turmaId", java.util.Optional.ofNullable(turmaId))
                .queryParamIfPresent("periodoLetivoId", java.util.Optional.ofNullable(periodoLetivoId))
                .queryParamIfPresent("status", java.util.Optional.ofNullable(status))
                .build()
                .toUriString();
        return webClient.get()
                .uri(uri)
                .header("Authorization", query.authorization())
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .exchangeToMono(response -> handle(response, "Enrollment document service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Enrollment document service indisponivel"));
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> buscarMatricula(
            UUID matriculaId,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.get()
                .uri("/internal/v1/matriculas/{matriculaId}", matriculaId)
                .header("Authorization", query.authorization())
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .exchangeToMono(response -> handle(response, "Enrollment document service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Enrollment document service indisponivel"));
    }
}

