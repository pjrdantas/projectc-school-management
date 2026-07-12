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
import br.com.escola.bff.application.usecase.ConsultarPessoaDetalheUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.people-read-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class PessoaDetailReadController {

    private final ConsultarPessoaDetalheUseCase consultarPessoaDetalheUseCase;

    public PessoaDetailReadController(ConsultarPessoaDetalheUseCase consultarPessoaDetalheUseCase) {
        this.consultarPessoaDetalheUseCase = consultarPessoaDetalheUseCase;
    }

    @GetMapping("/api/pessoas/{pessoaId}")
    public Mono<ResponseEntity<String>> buscarPessoaPorId(
            @PathVariable UUID pessoaId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarPessoaDetalheUseCase.buscarPessoaPorId(authorization, correlationId, pessoaId);
    }

    @GetMapping("/api/pessoas/{pessoaId}/endereco-principal")
    public Mono<ResponseEntity<String>> buscarEnderecoPrincipal(
            @PathVariable UUID pessoaId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarPessoaDetalheUseCase.buscarEnderecoPrincipal(authorization, correlationId, pessoaId);
    }

    @GetMapping("/api/pessoas/{pessoaId}/enderecos")
    public Mono<ResponseEntity<String>> listarEnderecos(
            @PathVariable UUID pessoaId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarPessoaDetalheUseCase.listarEnderecos(authorization, correlationId, pessoaId);
    }

    @GetMapping("/api/pessoas/{pessoaId}/contato")
    public Mono<ResponseEntity<String>> buscarContato(
            @PathVariable UUID pessoaId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarPessoaDetalheUseCase.buscarContato(authorization, correlationId, pessoaId);
    }

    @GetMapping("/api/pessoas/{pessoaId}/documentos")
    public Mono<ResponseEntity<String>> listarDocumentos(
            @PathVariable UUID pessoaId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarPessoaDetalheUseCase.listarDocumentos(authorization, correlationId, pessoaId);
    }

    @GetMapping("/api/documentos/{documentoId}")
    public Mono<ResponseEntity<String>> buscarDocumentoPorId(
            @PathVariable UUID documentoId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarPessoaDetalheUseCase.buscarDocumentoPorId(authorization, correlationId, documentoId);
    }
}
