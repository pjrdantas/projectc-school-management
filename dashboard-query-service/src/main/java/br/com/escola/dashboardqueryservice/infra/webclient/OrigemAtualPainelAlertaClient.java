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
import br.com.escola.dashboardqueryservice.application.dto.PainelAlertaResponse;
import br.com.escola.dashboardqueryservice.application.exception.PainelQueryServiceResourceNotFoundException;
import br.com.escola.dashboardqueryservice.application.exception.DownstreamUnavailableException;
import br.com.escola.dashboardqueryservice.application.port.out.PainelAlertaPort;

@Component
public class OrigemAtualPainelAlertaClient implements PainelAlertaPort {

    private final RestClient restClient;

    public OrigemAtualPainelAlertaClient(RestClient dashboardQueryMonolithRestClient) {
        this.restClient = dashboardQueryMonolithRestClient;
    }

    @Override
    public List<PainelAlertaResponse> consultar(
            String authorization,
            InternalRequestContext context,
            String publicoCodigo,
            UUID professorId) {
        try {
            List<PainelAlertaResponse> response = restClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder.path("/api/dashboard/alertas")
                                .queryParam("publicoCodigo", publicoCodigo);
                        if (professorId != null) {
                            builder.queryParam("professorId", professorId);
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
                    .body(new ParameterizedTypeReference<List<PainelAlertaResponse>>() {
                    });
            return response == null ? List.of() : response;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new PainelQueryServiceResourceNotFoundException(
                        "Alertas de dashboard nao encontrados no monolito");
            }
            throw exception;
        } catch (ResourceAccessException exception) {
            throw new DownstreamUnavailableException("Monolito indisponivel para consulta de alertas de dashboard", exception);
        }
    }
}

