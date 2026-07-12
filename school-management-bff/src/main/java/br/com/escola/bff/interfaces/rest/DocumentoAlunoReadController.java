package br.com.escola.bff.interfaces.rest;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.ConsultarDocumentoAlunoUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.enrollment-document-read-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class DocumentoAlunoReadController {

    private final ConsultarDocumentoAlunoUseCase consultarDocumentoAlunoUseCase;

    public DocumentoAlunoReadController(ConsultarDocumentoAlunoUseCase consultarDocumentoAlunoUseCase) {
        this.consultarDocumentoAlunoUseCase = consultarDocumentoAlunoUseCase;
    }

    @GetMapping("/api/documentos-alunos/alunos/{alunoId}")
    public Mono<ResponseEntity<String>> listarDocumentosPorAluno(
            @PathVariable UUID alunoId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarDocumentoAlunoUseCase.listarDocumentosPorAluno(authorization, correlationId, alunoId);
    }
}
