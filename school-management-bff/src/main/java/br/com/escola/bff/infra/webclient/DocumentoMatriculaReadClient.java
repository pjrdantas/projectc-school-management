package br.com.escola.bff.infra.webclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.DocumentoMatriculaReadPort;
import br.com.escola.bff.infra.config.DocumentoMatriculaServiceClientProperties;
import reactor.core.publisher.Mono;

@Component
public class DocumentoMatriculaReadClient extends AbstractDownstreamClientSupport
        implements DocumentoMatriculaReadPort {

    private final WebClient webClient;
    private final DocumentoMatriculaServiceClientProperties properties;

    public DocumentoMatriculaReadClient(
            @Qualifier("enrollmentDocumentServiceWebClient")
            WebClient enrollmentDocumentServiceWebClient,
            DocumentoMatriculaServiceClientProperties properties) {
        this.webClient = enrollmentDocumentServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> listarDocumentosPorEntidade(
            String entidadeTipo,
            UUID entidadeId,
            CatalogReadQuery query,
            AuthSessionContext context) {
        String uri = UriComponentsBuilder.fromPath("/internal/v1/documentos")
                .queryParam("entidadeTipo", entidadeTipo)
                .queryParam("entidadeId", entidadeId)
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
}

