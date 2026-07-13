package br.com.escola.bff.interfaces.rest;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.SalvarDiarioClasseUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.pedagogical-write-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class DiarioClasseWriteController {

    private final SalvarDiarioClasseUseCase salvarDiarioClasseUseCase;

    public DiarioClasseWriteController(SalvarDiarioClasseUseCase salvarDiarioClasseUseCase) {
        this.salvarDiarioClasseUseCase = salvarDiarioClasseUseCase;
    }

    @PutMapping("/api/diarios-classe/{idDiarioClasse}")
    public Mono<ResponseEntity<String>> salvar(
            @PathVariable String idDiarioClasse,
            @RequestBody String requestBody,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return salvarDiarioClasseUseCase.salvar(authorization, correlationId, idDiarioClasse, requestBody);
    }
}
