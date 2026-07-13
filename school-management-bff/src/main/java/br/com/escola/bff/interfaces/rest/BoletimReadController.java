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
import br.com.escola.bff.application.usecase.ConsultarBoletimUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.pedagogical-read-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class BoletimReadController {

    private final ConsultarBoletimUseCase consultarBoletimUseCase;

    public BoletimReadController(ConsultarBoletimUseCase consultarBoletimUseCase) {
        this.consultarBoletimUseCase = consultarBoletimUseCase;
    }

    @GetMapping("/api/matriculas/{matriculaId}/boletim")
    public Mono<ResponseEntity<String>> consultarBoletimPorMatricula(
            @PathVariable UUID matriculaId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarBoletimUseCase.consultarBoletimPorMatricula(
                authorization,
                correlationId,
                matriculaId);
    }

    @GetMapping("/api/matriculas/{matriculaId}/boletim/fechamentos")
    public Mono<ResponseEntity<String>> listarFechamentosPorMatricula(
            @PathVariable UUID matriculaId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarBoletimUseCase.listarFechamentosPorMatricula(
                authorization,
                correlationId,
                matriculaId);
    }
}
