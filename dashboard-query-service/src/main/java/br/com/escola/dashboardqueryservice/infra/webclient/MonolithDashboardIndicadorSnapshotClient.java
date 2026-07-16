package br.com.escola.dashboardqueryservice.infra.webclient;

import java.time.LocalDate;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.DashboardIndicadorSnapshotResponse;
import br.com.escola.dashboardqueryservice.application.exception.DashboardQueryServiceResourceNotFoundException;
import br.com.escola.dashboardqueryservice.application.exception.DownstreamUnavailableException;
import br.com.escola.dashboardqueryservice.application.port.out.DashboardIndicadorSnapshotPort;

@Component
public class MonolithDashboardIndicadorSnapshotClient implements DashboardIndicadorSnapshotPort {

    private final RestClient restClient;

    public MonolithDashboardIndicadorSnapshotClient(RestClient dashboardQueryMonolithRestClient) {
        this.restClient = dashboardQueryMonolithRestClient;
    }

    @Override
    public List<DashboardIndicadorSnapshotResponse> listarPorPublicoCodigo(
            String authorization,
            InternalRequestContext context,
            String publicoCodigo,
            LocalDate referenciaData) {
        try {
            List<DashboardIndicadorSnapshotResponse> response = restClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder.path("/api/dashboard/snapshots/publicos/{publicoCodigo}");
                        if (referenciaData != null) {
                            builder.queryParam("referenciaData", referenciaData);
                        }
                        return builder.build(publicoCodigo);
                    })
                    .headers(headers -> {
                        headers.set(HttpHeaders.AUTHORIZATION, authorization);
                        headers.set("X-Correlation-Id", context.correlationId());
                        headers.set("X-Usuario-Id", context.usuarioId().toString());
                        headers.set("X-Escola-Id", context.escolaId().toString());
                    })
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<DashboardIndicadorSnapshotResponse>>() {
                    });
            return response == null ? List.of() : response;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new DashboardQueryServiceResourceNotFoundException(exception.getResponseBodyAsString());
            }
            throw exception;
        } catch (ResourceAccessException exception) {
            throw new DownstreamUnavailableException("Monolito indisponivel na leitura de snapshots de dashboard.", exception);
        }
    }
}
