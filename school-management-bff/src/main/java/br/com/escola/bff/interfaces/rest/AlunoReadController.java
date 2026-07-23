package br.com.escola.bff.interfaces.rest;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.ConsultarAlunoUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.people-read-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class AlunoReadController {

    private final ConsultarAlunoUseCase consultarAlunoUseCase;

    public AlunoReadController(ConsultarAlunoUseCase consultarAlunoUseCase) {
        this.consultarAlunoUseCase = consultarAlunoUseCase;
    }

    @GetMapping("/api/alunos")
    public Mono<ResponseEntity<String>> listar(
            @RequestParam(required = false) String nome,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarAlunoUseCase.listar(authorization, correlationId, nome);
    }

    @GetMapping("/api/alunos/{alunoId}")
    public Mono<ResponseEntity<String>> buscarPorId(
            @PathVariable UUID alunoId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarAlunoUseCase.buscarPorId(authorization, correlationId, alunoId);
    }

    @GetMapping("/api/alunos/{alunoId}/ficha")
    public Mono<ResponseEntity<String>> buscarFicha(
            @PathVariable UUID alunoId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarAlunoUseCase.buscarFicha(authorization, correlationId, alunoId);
    }

    @PostMapping("/api/alunos")
    public Mono<ResponseEntity<String>> criar(
            @RequestBody String body,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarAlunoUseCase.encaminharEscrita(HttpMethod.POST, null, body, authorization, correlationId)
                .map(response -> response.getStatusCode().is2xxSuccessful()
                        ? ResponseEntity.status(HttpStatus.CREATED).headers(response.getHeaders()).body(response.getBody())
                        : response);
    }

    @PutMapping("/api/alunos/{alunoId}")
    public Mono<ResponseEntity<String>> atualizar(
            @PathVariable UUID alunoId,
            @RequestBody String body,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarAlunoUseCase.encaminharEscrita(HttpMethod.PUT, alunoId, body, authorization, correlationId);
    }

    @DeleteMapping("/api/alunos/{alunoId}")
    public Mono<ResponseEntity<String>> excluir(
            @PathVariable UUID alunoId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarAlunoUseCase.encaminharEscrita(HttpMethod.DELETE, alunoId, null, authorization, correlationId);
    }
}
