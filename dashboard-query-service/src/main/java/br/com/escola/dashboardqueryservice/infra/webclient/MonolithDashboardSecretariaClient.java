package br.com.escola.dashboardqueryservice.infra.webclient;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.DashboardSecretariaResponse;
import br.com.escola.dashboardqueryservice.application.exception.DashboardQueryServiceResourceNotFoundException;
import br.com.escola.dashboardqueryservice.application.exception.DownstreamUnavailableException;
import br.com.escola.dashboardqueryservice.application.port.out.DashboardSecretariaPort;

@Component
public class MonolithDashboardSecretariaClient implements DashboardSecretariaPort {

    private final RestClient restClient;

    public MonolithDashboardSecretariaClient(RestClient dashboardQueryMonolithRestClient) {
        this.restClient = dashboardQueryMonolithRestClient;
    }

    @Override
    public DashboardSecretariaResponse consultar(String authorization, InternalRequestContext context) {
        try {
            return restClient.get()
                    .uri("/api/dashboard/secretaria")
                    .headers(headers -> {
                        headers.set(HttpHeaders.AUTHORIZATION, authorization);
                        headers.set("X-Correlation-Id", context.correlationId());
                        headers.set("X-Usuario-Id", context.usuarioId().toString());
                        headers.set("X-Escola-Id", context.escolaId().toString());
                    })
                    .retrieve()
                    .body(DashboardSecretariaResponse.class);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new DashboardQueryServiceResourceNotFoundException(
                        "Dashboard secretaria nao encontrado no monolito");
            }
            throw exception;
        } catch (ResourceAccessException exception) {
            throw new DownstreamUnavailableException("Monolito indisponivel para consulta do dashboard secretaria", exception);
        }
    }
}
