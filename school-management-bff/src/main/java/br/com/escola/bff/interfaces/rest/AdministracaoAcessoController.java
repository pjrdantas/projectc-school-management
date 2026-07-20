package br.com.escola.bff.interfaces.rest;

import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.dto.AdministracaoAcessoCommand;
import br.com.escola.bff.application.model.OperacaoAcesso;
import br.com.escola.bff.application.model.RecursoAcesso;
import br.com.escola.bff.application.port.in.AdministrarAcessoUseCase;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class AdministracaoAcessoController {

    private final AdministrarAcessoUseCase administrarAcessoUseCase;

    public AdministracaoAcessoController(AdministrarAcessoUseCase administrarAcessoUseCase) {
        this.administrarAcessoUseCase = administrarAcessoUseCase;
    }

    @GetMapping("/{recurso:usuarios|perfis|permissoes}")
    public Mono<ResponseEntity<String>> listar(
            @PathVariable String recurso,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return executar(recurso, OperacaoAcesso.LISTAR, null, null, authorization, correlationId);
    }

    @GetMapping("/{recurso:usuarios|perfis|permissoes}/{id}")
    public Mono<ResponseEntity<String>> buscar(
            @PathVariable String recurso,
            @PathVariable UUID id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return executar(recurso, OperacaoAcesso.BUSCAR, id, null, authorization, correlationId);
    }

    @PostMapping(path = "/{recurso:usuarios|perfis|permissoes}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<String>> criar(
            @PathVariable String recurso,
            @RequestBody String requestBody,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return executar(recurso, OperacaoAcesso.CRIAR, null, requestBody, authorization, correlationId);
    }

    @PutMapping(path = "/{recurso:usuarios|perfis|permissoes}/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<String>> atualizar(
            @PathVariable String recurso,
            @PathVariable UUID id,
            @RequestBody String requestBody,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return executar(recurso, OperacaoAcesso.ATUALIZAR, id, requestBody, authorization, correlationId);
    }

    @DeleteMapping("/{recurso:usuarios|perfis|permissoes}/{id}")
    public Mono<ResponseEntity<String>> excluir(
            @PathVariable String recurso,
            @PathVariable UUID id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return executar(recurso, OperacaoAcesso.EXCLUIR, id, null, authorization, correlationId);
    }

    private Mono<ResponseEntity<String>> executar(
            String recurso,
            OperacaoAcesso operacao,
            UUID recursoId,
            String requestBody,
            String authorization,
            String correlationId) {
        return administrarAcessoUseCase.executar(new AdministracaoAcessoCommand(
                RecursoAcesso.valueOf(recurso.toUpperCase()),
                operacao,
                recursoId,
                requestBody,
                authorization,
                correlationId));
    }
}
