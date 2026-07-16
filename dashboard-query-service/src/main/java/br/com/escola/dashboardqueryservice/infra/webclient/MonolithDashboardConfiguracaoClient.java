package br.com.escola.dashboardqueryservice.infra.webclient;

import java.util.List;
import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.DashboardConfiguracaoResponse;
import br.com.escola.dashboardqueryservice.application.exception.DashboardQueryServiceResourceNotFoundException;
import br.com.escola.dashboardqueryservice.application.exception.DownstreamUnavailableException;
import br.com.escola.dashboardqueryservice.application.port.out.DashboardConfiguracaoPort;

@Component
public class MonolithDashboardConfiguracaoClient implements DashboardConfiguracaoPort {

    private final RestClient restClient;

    public MonolithDashboardConfiguracaoClient(RestClient dashboardQueryMonolithRestClient) {
        this.restClient = dashboardQueryMonolithRestClient;
    }

    @Override
    public List<DashboardConfiguracaoResponse> listarDashboards(
            String authorization,
            InternalRequestContext context,
            UUID publicoDashboardId,
            String publicoCodigo) {
        try {
            List<DashboardConfiguracaoResponse> response = restClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder.path("/api/dashboard/configuracoes/dashboards");
                        if (publicoDashboardId != null) {
                            builder.queryParam("publicoDashboardId", publicoDashboardId);
                        }
                        if (publicoCodigo != null) {
                            builder.queryParam("publicoCodigo", publicoCodigo);
                        }
                        return builder.build();
                    })
                    .headers(headers -> {
                        headers.set(HttpHeaders.AUTHORIZATION, authorization);
                        headers.set("X-Correlation-Id", context.correlationId());
                        headers.set("X-Usuario-Id", context.usuarioId().toString());
                        headers.set("X-Escola-Id", context.escolaId().toString());
                    })
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<DashboardConfiguracaoResponse>>() {
                    });
            return response == null ? List.of() : response;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new DashboardQueryServiceResourceNotFoundException(exception.getResponseBodyAsString());
            }
            throw exception;
        } catch (ResourceAccessException exception) {
            throw new DownstreamUnavailableException("Monolito indisponivel na leitura de dashboards de configuracao.", exception);
        }
    }
}
