package br.com.escola.bff.interfaces.rest;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.ConsultarPessoaCatalogoUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.people-read-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class PessoaCatalogReadController {

    private final ConsultarPessoaCatalogoUseCase consultarPessoaCatalogoUseCase;

    public PessoaCatalogReadController(ConsultarPessoaCatalogoUseCase consultarPessoaCatalogoUseCase) {
        this.consultarPessoaCatalogoUseCase = consultarPessoaCatalogoUseCase;
    }

    @GetMapping("/api/pessoas/catalogos/tipos-pessoa")
    public Mono<ResponseEntity<String>> listarTiposPessoa(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarPessoaCatalogoUseCase.listarTiposPessoa(authorization, correlationId);
    }

    @GetMapping("/api/pessoas/catalogos/tipos-endereco")
    public Mono<ResponseEntity<String>> listarTiposEndereco(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarPessoaCatalogoUseCase.listarTiposEndereco(authorization, correlationId);
    }

    @GetMapping("/api/pessoas/catalogos/status-aluno")
    public Mono<ResponseEntity<String>> listarStatusAluno(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarPessoaCatalogoUseCase.listarStatusAluno(authorization, correlationId);
    }

    @GetMapping("/api/pessoas/catalogos/parentescos")
    public Mono<ResponseEntity<String>> listarParentescos(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarPessoaCatalogoUseCase.listarParentescos(authorization, correlationId);
    }
}
