package br.com.escola.bff.interfaces.rest;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.ConsultarBibliotecaConteudoPedagogicoUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.planning-ai-read-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class BibliotecaConteudoPedagogicoReadController {

    private final ConsultarBibliotecaConteudoPedagogicoUseCase consultarBibliotecaConteudoPedagogicoUseCase;

    public BibliotecaConteudoPedagogicoReadController(
            ConsultarBibliotecaConteudoPedagogicoUseCase consultarBibliotecaConteudoPedagogicoUseCase) {
        this.consultarBibliotecaConteudoPedagogicoUseCase = consultarBibliotecaConteudoPedagogicoUseCase;
    }

    @GetMapping("/api/biblioteca-conteudos-pedagogicos")
    public Mono<ResponseEntity<String>> listarBiblioteca(
            @RequestParam(required = false) UUID professorId,
            @RequestParam(required = false) UUID disciplinaId,
            @RequestParam(required = false) String tipoConteudo,
            @RequestParam(required = false) String tema,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return consultarBibliotecaConteudoPedagogicoUseCase.listarBiblioteca(
                authorization,
                correlationId,
                professorId,
                disciplinaId,
                tipoConteudo,
                tema);
    }
}
