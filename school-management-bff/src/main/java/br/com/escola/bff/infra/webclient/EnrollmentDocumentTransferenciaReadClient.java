package br.com.escola.bff.infra.webclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.EnrollmentDocumentTransferenciaReadPort;
import br.com.escola.bff.infra.config.EnrollmentDocumentServiceClientProperties;
import reactor.core.publisher.Mono;

@Component
public class EnrollmentDocumentTransferenciaReadClient extends AbstractDownstreamClientSupport
        implements EnrollmentDocumentTransferenciaReadPort {

    private final WebClient webClient;
    private final EnrollmentDocumentServiceClientProperties properties;

    public EnrollmentDocumentTransferenciaReadClient(
            @Qualifier("enrollmentDocumentServiceWebClient")
            WebClient enrollmentDocumentServiceWebClient,
            EnrollmentDocumentServiceClientProperties properties) {
        this.webClient = enrollmentDocumentServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> listarTransferenciasPorAluno(
            UUID alunoId,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.get()
                .uri("/internal/v1/transferencias/alunos/{alunoId}", alunoId)
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
    public Mono<org.springframework.http.ResponseEntity<String>> buscarTransferenciaPorId(
            UUID transferenciaId,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.get()
                .uri("/internal/v1/transferencias/{transferenciaId}", transferenciaId)
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
