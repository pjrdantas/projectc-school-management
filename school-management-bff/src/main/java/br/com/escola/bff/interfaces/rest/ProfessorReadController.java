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
import br.com.escola.bff.application.usecase.ConsultarProfessorUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.people-read-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class ProfessorReadController {

    private final ConsultarProfessorUseCase consultarProfessorUseCase;

    public ProfessorReadController(ConsultarProfessorUseCase consultarProfessorUseCase) {
        this.consultarProfessorUseCase = consultarProfessorUseCase;
    }

    @GetMapping("/api/professores")
    public Mono<ResponseEntity<String>> listarProfessores(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarProfessorUseCase.listarProfessores(authorization, correlationId);
    }

    @GetMapping("/api/professores/{professorId}")
    public Mono<ResponseEntity<String>> buscarProfessorPorId(
            @PathVariable UUID professorId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarProfessorUseCase.buscarProfessorPorId(authorization, correlationId, professorId);
    }

    @GetMapping("/api/professores/{professorId}/turmas-disciplinas")
    public Mono<ResponseEntity<String>> listarAlocacoesPorProfessor(
            @PathVariable UUID professorId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarProfessorUseCase.listarAlocacoesPorProfessor(authorization, correlationId, professorId);
    }

    @GetMapping("/api/turmas/{turmaId}/professores")
    public Mono<ResponseEntity<String>> listarProfessoresPorTurma(
            @PathVariable UUID turmaId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarProfessorUseCase.listarProfessoresPorTurma(authorization, correlationId, turmaId);
    }

    @GetMapping("/api/professores/funcionarios-elegiveis")
    public Mono<ResponseEntity<String>> listarFuncionariosElegiveis(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarProfessorUseCase.listarFuncionariosElegiveis(authorization, correlationId);
    }
}
