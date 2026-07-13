package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.EnrollmentDocumentReadPort;
import br.com.escola.bff.application.usecase.ConsultarDocumentoUseCase;
import reactor.core.publisher.Mono;

public class DocumentoReadProxyService implements ConsultarDocumentoUseCase {

    private final AuthContextPort authContextPort;
    private final EnrollmentDocumentReadPort enrollmentDocumentReadPort;

    public DocumentoReadProxyService(
            AuthContextPort authContextPort,
            EnrollmentDocumentReadPort enrollmentDocumentReadPort) {
        this.authContextPort = authContextPort;
        this.enrollmentDocumentReadPort = enrollmentDocumentReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> listarDocumentosPorEntidade(
            String authorization,
            String correlationId,
            String entidadeTipo,
            UUID entidadeId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> enrollmentDocumentReadPort.listarDocumentosPorEntidade(
                        entidadeTipo,
                        entidadeId,
                        query,
                        context));
    }
}
