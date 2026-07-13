package br.com.escola.bff.interfaces.rest;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.AprovarPlanejamentoIaConteudoVersaoUseCase;
import br.com.escola.bff.application.usecase.CriarPlanejamentoIaConteudoUseCase;
import br.com.escola.bff.application.usecase.CriarPlanejamentoIaConteudoVersaoUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.planning-ai-write-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class PlanejamentoIaConteudoWriteController {

    private final CriarPlanejamentoIaConteudoUseCase criarPlanejamentoIaConteudoUseCase;
    private final CriarPlanejamentoIaConteudoVersaoUseCase criarPlanejamentoIaConteudoVersaoUseCase;
    private final AprovarPlanejamentoIaConteudoVersaoUseCase aprovarPlanejamentoIaConteudoVersaoUseCase;

    public PlanejamentoIaConteudoWriteController(
            CriarPlanejamentoIaConteudoUseCase criarPlanejamentoIaConteudoUseCase,
            CriarPlanejamentoIaConteudoVersaoUseCase criarPlanejamentoIaConteudoVersaoUseCase,
            AprovarPlanejamentoIaConteudoVersaoUseCase aprovarPlanejamentoIaConteudoVersaoUseCase) {
        this.criarPlanejamentoIaConteudoUseCase = criarPlanejamentoIaConteudoUseCase;
        this.criarPlanejamentoIaConteudoVersaoUseCase = criarPlanejamentoIaConteudoVersaoUseCase;
        this.aprovarPlanejamentoIaConteudoVersaoUseCase = aprovarPlanejamentoIaConteudoVersaoUseCase;
    }

    @PostMapping("/api/planejamentos-bimestrais/{planejamentoId}/ia/conteudos")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<String>> criarConteudo(
            @PathVariable UUID planejamentoId,
            @RequestBody String requestBody,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return criarPlanejamentoIaConteudoUseCase.executar(
                authorization,
                correlationId,
                planejamentoId,
                requestBody);
    }

    @PostMapping("/api/ia/conteudos/{conteudoId}/versoes")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<String>> criarVersao(
            @PathVariable UUID conteudoId,
            @RequestBody String requestBody,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return criarPlanejamentoIaConteudoVersaoUseCase.executar(
                authorization,
                correlationId,
                conteudoId,
                requestBody);
    }

    @org.springframework.web.bind.annotation.PatchMapping("/api/ia/conteudos/{conteudoId}/aprovar-versao")
    public Mono<ResponseEntity<String>> aprovarVersao(
            @PathVariable UUID conteudoId,
            @RequestBody String requestBody,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return aprovarPlanejamentoIaConteudoVersaoUseCase.executar(
                authorization,
                correlationId,
                conteudoId,
                requestBody);
    }
}
