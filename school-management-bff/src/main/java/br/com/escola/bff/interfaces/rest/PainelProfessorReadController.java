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
import br.com.escola.bff.application.usecase.ConsultarPainelProfessorUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.dashboard-read-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class PainelProfessorReadController {

    private final ConsultarPainelProfessorUseCase consultarPainelProfessorUseCase;

    public PainelProfessorReadController(ConsultarPainelProfessorUseCase consultarPainelProfessorUseCase) {
        this.consultarPainelProfessorUseCase = consultarPainelProfessorUseCase;
    }

    @GetMapping("/api/dashboard/professores/{professorId}")
    public Mono<ResponseEntity<String>> consultar(
            @PathVariable UUID professorId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarPainelProfessorUseCase.consultar(authorization, correlationId, professorId);
    }
}

