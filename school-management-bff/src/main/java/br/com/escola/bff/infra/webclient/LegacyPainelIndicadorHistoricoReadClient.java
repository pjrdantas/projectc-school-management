package br.com.escola.bff.infra.webclient;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.LegacyPainelIndicadorHistoricoReadPort;
import reactor.core.publisher.Mono;

@Component
public class LegacyPainelIndicadorHistoricoReadClient extends AbstractDownstreamClientSupport
        implements LegacyPainelIndicadorHistoricoReadPort {

    private final WebClient webClient;

    public LegacyPainelIndicadorHistoricoReadClient(
            @Qualifier("monolithWebClient")
            WebClient monolithWebClient) {
        this.webClient = monolithWebClient;
    }

    @Override
    public Mono<org.springframework.http.ResponseEntity<String>> consultarHistorico(
            String publicoCodigo,
            String codigoIndicador,
            LocalDate dataInicio,
            LocalDate dataFim,
            UUID professorId,
            CatalogReadQuery query) {
        return webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/api/dashboard/snapshots/historico/publicos/{publicoCodigo}");
                    if (codigoIndicador != null) {
                        builder.queryParam("codigoIndicador", codigoIndicador);
                    }
                    if (dataInicio != null) {
                        builder.queryParam("dataInicio", dataInicio);
                    }
                    if (dataFim != null) {
                        builder.queryParam("dataFim", dataFim);
                    }
                    if (professorId != null) {
                        builder.queryParam("professorId", professorId);
                    }
                    return builder.build(publicoCodigo);
                })
                .header(HttpHeaders.AUTHORIZATION, query.authorization())
                .header(TrustedHeaders.CORRELATION_ID, query.correlationId())
                .exchangeToMono(response -> handle(response, "Monolito indisponivel para historico de snapshots"))
                .onErrorMap(error -> mapTransportError(error, "Monolito indisponivel para historico de snapshots"));
    }
}

