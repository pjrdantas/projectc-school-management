package br.com.escola.bff.interfaces.rest;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.ConsultarMatriculaUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.enrollment-document-read-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class MatriculaReadController {

    private final ConsultarMatriculaUseCase consultarMatriculaUseCase;

    public MatriculaReadController(ConsultarMatriculaUseCase consultarMatriculaUseCase) {
        this.consultarMatriculaUseCase = consultarMatriculaUseCase;
    }

    @GetMapping("/api/matriculas")
    public Mono<ResponseEntity<String>> listarMatriculas(
            @RequestParam(required = false) UUID alunoId,
            @RequestParam(required = false) UUID turmaId,
            @RequestParam(required = false) UUID periodoLetivoId,
            @RequestParam(required = false) String status,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarMatriculaUseCase.listarMatriculas(
                authorization,
                correlationId,
                alunoId,
                turmaId,
                periodoLetivoId,
                status);
    }

    @GetMapping("/api/matriculas/{matriculaId}")
    public Mono<ResponseEntity<String>> buscarMatricula(
            @PathVariable UUID matriculaId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarMatriculaUseCase.buscarMatricula(authorization, correlationId, matriculaId);
    }
}
