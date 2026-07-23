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
import br.com.escola.bff.application.usecase.ConsultarHistoricoEscolarUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.pedagogical-read-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class HistoricoEscolarReadController {

    private final ConsultarHistoricoEscolarUseCase consultarHistoricoEscolarUseCase;

    public HistoricoEscolarReadController(ConsultarHistoricoEscolarUseCase consultarHistoricoEscolarUseCase) {
        this.consultarHistoricoEscolarUseCase = consultarHistoricoEscolarUseCase;
    }

    @GetMapping("/api/historicos-escolares")
    public Mono<ResponseEntity<String>> listar(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarHistoricoEscolarUseCase.listar(authorization, correlationId, page, size);
    }

    @GetMapping("/api/historicos-escolares/alunos/{alunoId}")
    public Mono<ResponseEntity<String>> listarPorAluno(
            @PathVariable UUID alunoId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarHistoricoEscolarUseCase.listarPorAluno(authorization, correlationId, alunoId);
    }

    @GetMapping("/api/historicos-escolares/novo")
    public Mono<ResponseEntity<String>> carregarNovo(
            @RequestParam UUID idAluno,
            @RequestParam UUID idMatricula,
            @RequestParam(defaultValue = "CADASTRO") String modo,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarHistoricoEscolarUseCase.carregarNovo(
                authorization,
                correlationId,
                idAluno,
                idMatricula,
                modo);
    }

    @GetMapping("/api/historicos-escolares/{id}/carregamento")
    public Mono<ResponseEntity<String>> carregarParaEdicao(
            @PathVariable UUID id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarHistoricoEscolarUseCase.carregarParaEdicao(authorization, correlationId, id);
    }
}
