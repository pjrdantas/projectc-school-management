package br.com.escola.bff.infra.webclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.LegacyBoletimReadPort;
import reactor.core.publisher.Mono;

@Component
public class LegacyBoletimReadClient extends AbstractDownstreamClientSupport implements LegacyBoletimReadPort {

    private final WebClient webClient;

    public LegacyBoletimReadClient(
            @Qualifier("monolithWebClient")
            WebClient monolithWebClient) {
        this.webClient = monolithWebClient;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> consultarBoletimPorMatricula(
            UUID matriculaId,
            CatalogReadQuery query) {
        return webClient.get()
                .uri("/api/matriculas/{matriculaId}/boletim", matriculaId)
                .header(HttpHeaders.AUTHORIZATION, query.authorization())
                .header(TrustedHeaders.CORRELATION_ID, query.correlationId())
                .exchangeToMono(response -> handle(response, "Monolito indisponivel para boletim"))
                .onErrorMap(error -> mapTransportError(error, "Monolito indisponivel para boletim"));
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> listarFechamentosPorMatricula(
            UUID matriculaId,
            CatalogReadQuery query) {
        return webClient.get()
                .uri("/api/matriculas/{matriculaId}/boletim/fechamentos", matriculaId)
                .header(HttpHeaders.AUTHORIZATION, query.authorization())
                .header(TrustedHeaders.CORRELATION_ID, query.correlationId())
                .exchangeToMono(response -> handle(response, "Monolito indisponivel para fechamentos de boletim"))
                .onErrorMap(error -> mapTransportError(error, "Monolito indisponivel para fechamentos de boletim"));
    }
}

