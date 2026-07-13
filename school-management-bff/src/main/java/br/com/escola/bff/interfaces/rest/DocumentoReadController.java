package br.com.escola.bff.interfaces.rest;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.ConsultarDocumentoUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.enrollment-document-read-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class DocumentoReadController {

    private final ConsultarDocumentoUseCase consultarDocumentoUseCase;

    public DocumentoReadController(ConsultarDocumentoUseCase consultarDocumentoUseCase) {
        this.consultarDocumentoUseCase = consultarDocumentoUseCase;
    }

    @GetMapping("/api/documentos")
    public Mono<ResponseEntity<String>> listarDocumentosPorEntidade(
            @RequestParam String entidadeTipo,
            @RequestParam UUID entidadeId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarDocumentoUseCase.listarDocumentosPorEntidade(
                authorization,
                correlationId,
                entidadeTipo,
                entidadeId);
    }
}
