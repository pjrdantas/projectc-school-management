package br.com.escola.bff.infra.webclient;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.TenantAtivoReadPort;
import br.com.escola.bff.infra.config.TenantAtivoServiceClientProperties;
import reactor.core.publisher.Mono;

@Component
public class TenantAtivoReadClient extends AbstractDownstreamClientSupport implements TenantAtivoReadPort {

    private final WebClient webClient;
    private final TenantAtivoServiceClientProperties properties;

    public TenantAtivoReadClient(
            @Qualifier("institutionalTenantServiceWebClient")
            WebClient institutionalTenantServiceWebClient,
            TenantAtivoServiceClientProperties properties) {
        this.webClient = institutionalTenantServiceWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<ResponseEntity<String>> listarEscolasDisponiveis(CatalogReadQuery query, AuthSessionContext context) {
        return webClient.get()
                .uri("/internal/v1/tenant/escolas")
                .header(HttpHeaders.AUTHORIZATION, query.authorization())
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .exchangeToMono(response -> handle(response, "Institutional tenant service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Institutional tenant service indisponivel"));
    }

    @Override
    public Mono<ResponseEntity<String>> consultarTenantAtivo(CatalogReadQuery query, AuthSessionContext context) {
        return webClient.get()
                .uri("/internal/v1/tenant/ativa")
                .header(HttpHeaders.AUTHORIZATION, query.authorization())
                .header("X-Internal-Token", properties.internalToken())
                .header("X-Correlation-Id", query.correlationId())
                .header("X-Usuario-Id", context.usuarioId().toString())
                .header("X-Escola-Id", context.escolaId().toString())
                .exchangeToMono(response -> handle(response, "Institutional tenant service retornou erro interno"))
                .timeout(properties.responseTimeout())
                .onErrorMap(error -> mapTransportError(error, "Institutional tenant service indisponivel"));
    }
}

