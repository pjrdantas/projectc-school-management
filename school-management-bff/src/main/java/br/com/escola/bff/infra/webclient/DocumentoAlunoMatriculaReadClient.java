package br.com.escola.bff.infra.webclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.DocumentoAlunoMatriculaReadPort;
import br.com.escola.bff.infra.config.DocumentoMatriculaServiceClientProperties;
import reactor.core.publisher.Mono;

@Component
public class DocumentoAlunoMatriculaReadClient extends AbstractDownstreamClientSupport
        implements DocumentoAlunoMatriculaReadPort {

    private final WebClient webClient;
    private final DocumentoMatriculaServiceClientProperties properties;

    public DocumentoAlunoMatriculaReadClient(
            @Qualifier("enrollmentDocumentServiceWebClient")
            WebClient enrollmentDocumentServiceWebClient,
            DocumentoMatriculaServiceClientProperties properties) {
        this.webClient = enrollmentDocumentServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> listarDocumentosPorAluno(
            UUID alunoId,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.get()
                .uri("/internal/v1/documentos-alunos/alunos/{alunoId}", alunoId)
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
    public Mono<org.springframework.http.ResponseEntity<String>> buscarDocumentoAlunoPorId(
            UUID id,
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.get()
                .uri("/internal/v1/documentos-alunos/{id}", id)
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

