package br.com.escola.bff.infra.webclient;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.PessoaCatalogoReadPort;
import br.com.escola.bff.infra.config.CadastroPessoaClientProperties;
import reactor.core.publisher.Mono;

@Component
public class PessoaCatalogoReadClient extends AbstractDownstreamClientSupport implements PessoaCatalogoReadPort {

    private final WebClient webClient;
    private final CadastroPessoaClientProperties properties;

    public PessoaCatalogoReadClient(
            @Qualifier("peopleServiceWebClient")
            WebClient peopleServiceWebClient,
            CadastroPessoaClientProperties properties) {
        this.webClient = peopleServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> listarTiposPessoa(
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.get()
                .uri("/internal/v1/pessoas/catalogos/tipos-pessoa")
                .header("Authorization", query.authorization())
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .exchangeToMono(response -> handle(response, "People service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "People service indisponivel"));
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> listarTiposEndereco(
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.get()
                .uri("/internal/v1/pessoas/catalogos/tipos-endereco")
                .header("Authorization", query.authorization())
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .exchangeToMono(response -> handle(response, "People service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "People service indisponivel"));
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> listarStatusAluno(
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.get()
                .uri("/internal/v1/pessoas/catalogos/status-aluno")
                .header("Authorization", query.authorization())
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .exchangeToMono(response -> handle(response, "People service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "People service indisponivel"));
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> listarParentescos(
            CatalogReadQuery query,
            AuthSessionContext context) {
        return webClient.get()
                .uri("/internal/v1/pessoas/catalogos/parentescos")
                .header("Authorization", query.authorization())
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .exchangeToMono(response -> handle(response, "People service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "People service indisponivel"));
    }
}


