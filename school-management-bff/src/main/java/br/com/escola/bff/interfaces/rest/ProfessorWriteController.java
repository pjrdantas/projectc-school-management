package br.com.escola.bff.interfaces.rest;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.ProfessorWriteUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.academic-professor-write-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class ProfessorWriteController {

    private final ProfessorWriteUseCase professorWriteUseCase;

    public ProfessorWriteController(ProfessorWriteUseCase professorWriteUseCase) {
        this.professorWriteUseCase = professorWriteUseCase;
    }

    @PostMapping("/api/professores")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<String>> criarProfessor(
            @RequestBody String requestBody,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return professorWriteUseCase.criarProfessor(authorization, correlationId, requestBody);
    }

    @PostMapping("/api/professores/{professorId}/turmas-disciplinas")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<String>> criarAlocacao(
            @PathVariable UUID professorId,
            @RequestBody String requestBody,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return professorWriteUseCase.criarAlocacao(authorization, correlationId, professorId, requestBody);
    }

    @PutMapping("/api/professores/{professorId}")
    public Mono<ResponseEntity<String>> atualizarProfessor(
            @PathVariable UUID professorId,
            @RequestBody String requestBody,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return professorWriteUseCase.atualizarProfessor(authorization, correlationId, professorId, requestBody);
    }

    @PutMapping("/api/professores/{professorId}/turmas-disciplinas/{alocacaoId}")
    public Mono<ResponseEntity<String>> atualizarAlocacao(
            @PathVariable UUID professorId,
            @PathVariable UUID alocacaoId,
            @RequestBody String requestBody,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return professorWriteUseCase.atualizarAlocacao(
                authorization, correlationId, professorId, alocacaoId, requestBody);
    }

    @DeleteMapping("/api/professores/{professorId}/turmas-disciplinas/{alocacaoId}")
    public Mono<ResponseEntity<String>> encerrarAlocacao(
            @PathVariable UUID professorId,
            @PathVariable UUID alocacaoId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return professorWriteUseCase.encerrarAlocacao(authorization, correlationId, professorId, alocacaoId);
    }
}
