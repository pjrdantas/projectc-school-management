package br.com.escola.bff.infra.webclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.LegacyResponsavelReadPort;
import reactor.core.publisher.Mono;

@Component
public class LegacyResponsavelReadClient extends AbstractDownstreamClientSupport implements LegacyResponsavelReadPort {

    private final WebClient webClient;

    public LegacyResponsavelReadClient(
            @Qualifier("monolithWebClient")
            WebClient monolithWebClient) {
        this.webClient = monolithWebClient;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> listarResponsaveis(
            String nome,
            String cpf,
            CatalogReadQuery query) {
        return webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/api/responsaveis");
                    if (nome != null) {
                        builder.queryParam("nome", nome);
                    }
                    if (cpf != null) {
                        builder.queryParam("cpf", cpf);
                    }
                    return builder.build();
                })
                .header(HttpHeaders.AUTHORIZATION, query.authorization())
                .header(TrustedHeaders.CORRELATION_ID, query.correlationId())
                .exchangeToMono(response -> handle(response, "Monolito indisponivel para listagem de responsaveis"))
                .onErrorMap(error -> mapTransportError(error, "Monolito indisponivel para listagem de responsaveis"));
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> buscarResponsavelPorId(
            UUID responsavelId,
            CatalogReadQuery query) {
        return webClient.get()
                .uri("/api/responsaveis/{id}", responsavelId)
                .header(HttpHeaders.AUTHORIZATION, query.authorization())
                .header(TrustedHeaders.CORRELATION_ID, query.correlationId())
                .exchangeToMono(response -> handle(response, "Monolito indisponivel para detalhe de responsavel"))
                .onErrorMap(error -> mapTransportError(error, "Monolito indisponivel para detalhe de responsavel"));
    }
}

