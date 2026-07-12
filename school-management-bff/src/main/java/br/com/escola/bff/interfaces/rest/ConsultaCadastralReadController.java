package br.com.escola.bff.interfaces.rest;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.ConsultarCadastroPessoaUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.people-read-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class ConsultaCadastralReadController {

    private final ConsultarCadastroPessoaUseCase consultarCadastroPessoaUseCase;

    public ConsultaCadastralReadController(ConsultarCadastroPessoaUseCase consultarCadastroPessoaUseCase) {
        this.consultarCadastroPessoaUseCase = consultarCadastroPessoaUseCase;
    }

    @GetMapping("/api/consulta-cadastral")
    public Mono<ResponseEntity<String>> consultar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId,
            @RequestParam(name = "nomeAluno", required = false) String nomeAluno,
            @RequestParam(name = "cpfAluno", required = false) String cpfAluno,
            @RequestParam(name = "nomeResponsavel", required = false) String nomeResponsavel,
            @RequestParam(name = "cpfResponsavel", required = false) String cpfResponsavel,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return consultarCadastroPessoaUseCase.consultar(
                authorization,
                correlationId,
                nomeAluno,
                cpfAluno,
                nomeResponsavel,
                cpfResponsavel,
                page,
                size);
    }
}
