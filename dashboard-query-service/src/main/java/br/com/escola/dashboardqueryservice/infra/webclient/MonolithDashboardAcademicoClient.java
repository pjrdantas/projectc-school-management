package br.com.escola.dashboardqueryservice.infra.webclient;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.DashboardAcademicoResponse;
import br.com.escola.dashboardqueryservice.application.exception.DashboardQueryServiceResourceNotFoundException;
import br.com.escola.dashboardqueryservice.application.exception.DownstreamUnavailableException;
import br.com.escola.dashboardqueryservice.application.port.out.DashboardAcademicoPort;

@Component
public class MonolithDashboardAcademicoClient implements DashboardAcademicoPort {

    private final RestClient restClient;

    public MonolithDashboardAcademicoClient(RestClient dashboardQueryMonolithRestClient) {
        this.restClient = dashboardQueryMonolithRestClient;
    }

    @Override
    public DashboardAcademicoResponse consultar(String authorization, InternalRequestContext context) {
        try {
            return restClient.get()
                    .uri("/api/dashboard/academico")
                    .headers(headers -> {
                        headers.set(HttpHeaders.AUTHORIZATION, authorization);
                        headers.set("X-Correlation-Id", context.correlationId());
                        headers.set("X-Usuario-Id", context.usuarioId().toString());
                        headers.set("X-Escola-Id", context.escolaId().toString());
                    })
                    .retrieve()
                    .body(DashboardAcademicoResponse.class);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new DashboardQueryServiceResourceNotFoundException(
                        "Dashboard academico nao encontrado no monolito");
            }
            throw exception;
        } catch (ResourceAccessException exception) {
            throw new DownstreamUnavailableException("Monolito indisponivel para consulta do dashboard academico", exception);
        }
    }
}
