package br.com.escola.dashboardqueryservice.infra.webclient;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.DashboardIndicadorHistoricoResponse;
import br.com.escola.dashboardqueryservice.application.exception.DashboardQueryServiceResourceNotFoundException;
import br.com.escola.dashboardqueryservice.application.exception.DownstreamUnavailableException;
import br.com.escola.dashboardqueryservice.application.port.out.DashboardIndicadorHistoricoPort;

@Component
public class MonolithDashboardIndicadorHistoricoClient implements DashboardIndicadorHistoricoPort {

    private final RestClient restClient;

    public MonolithDashboardIndicadorHistoricoClient(RestClient dashboardQueryMonolithRestClient) {
        this.restClient = dashboardQueryMonolithRestClient;
    }

    @Override
    public List<DashboardIndicadorHistoricoResponse> consultarHistorico(
            String authorization,
            InternalRequestContext context,
            String publicoCodigo,
            String codigoIndicador,
            LocalDate dataInicio,
            LocalDate dataFim,
            UUID professorId) {
        try {
            List<DashboardIndicadorHistoricoResponse> response = restClient.get()
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
                    .headers(headers -> {
                        headers.set(HttpHeaders.AUTHORIZATION, authorization);
                        headers.set("X-Correlation-Id", context.correlationId());
                        headers.set("X-Usuario-Id", context.usuarioId().toString());
                        headers.set("X-Escola-Id", context.escolaId().toString());
                    })
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<DashboardIndicadorHistoricoResponse>>() {
                    });
            return response == null ? List.of() : response;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new DashboardQueryServiceResourceNotFoundException(exception.getResponseBodyAsString());
            }
            throw exception;
        } catch (ResourceAccessException exception) {
            throw new DownstreamUnavailableException("Monolito indisponivel na leitura de historico de snapshots.", exception);
        }
    }
}
