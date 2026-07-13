package br.com.escola.bff.interfaces.rest;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.CriarEscolaOrigemUseCase;
import br.com.escola.bff.application.usecase.CriarTransferenciaUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.enrollment-document-write-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class EnrollmentDocumentWriteController {

    private final CriarEscolaOrigemUseCase criarEscolaOrigemUseCase;
    private final CriarTransferenciaUseCase criarTransferenciaUseCase;

    public EnrollmentDocumentWriteController(
            CriarEscolaOrigemUseCase criarEscolaOrigemUseCase,
            CriarTransferenciaUseCase criarTransferenciaUseCase) {
        this.criarEscolaOrigemUseCase = criarEscolaOrigemUseCase;
        this.criarTransferenciaUseCase = criarTransferenciaUseCase;
    }

    @PostMapping("/api/escolas-origem")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<String>> criarEscolaOrigem(
            @RequestBody String requestBody,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return criarEscolaOrigemUseCase.executar(authorization, correlationId, requestBody);
    }

    @PostMapping("/api/transferencias")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<String>> criarTransferencia(
            @RequestBody String requestBody,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return criarTransferenciaUseCase.executar(authorization, correlationId, requestBody);
    }
}
