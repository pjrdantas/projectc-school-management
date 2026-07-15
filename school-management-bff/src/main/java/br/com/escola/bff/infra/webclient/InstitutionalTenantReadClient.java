package br.com.escola.bff.infra.webclient;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.InstitutionalTenantReadPort;
import br.com.escola.bff.infra.config.InstitutionalTenantServiceClientProperties;
import reactor.core.publisher.Mono;

@Component
public class InstitutionalTenantReadClient extends AbstractDownstreamClientSupport implements InstitutionalTenantReadPort {

    private final WebClient webClient;
    private final InstitutionalTenantServiceClientProperties properties;

    public InstitutionalTenantReadClient(
            @Qualifier("institutionalTenantServiceWebClient")
            WebClient institutionalTenantServiceWebClient,
            InstitutionalTenantServiceClientProperties properties) {
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
