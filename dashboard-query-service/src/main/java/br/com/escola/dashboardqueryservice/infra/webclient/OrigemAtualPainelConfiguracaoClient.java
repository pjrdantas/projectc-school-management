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
import br.com.escola.dashboardqueryservice.application.dto.PainelConfiguracaoResponse;
import br.com.escola.dashboardqueryservice.application.exception.PainelQueryServiceResourceNotFoundException;
import br.com.escola.dashboardqueryservice.application.exception.DownstreamUnavailableException;
import br.com.escola.dashboardqueryservice.application.port.out.PainelConfiguracaoPort;

@Component
public class OrigemAtualPainelConfiguracaoClient implements PainelConfiguracaoPort {

    private final RestClient restClient;

    public OrigemAtualPainelConfiguracaoClient(RestClient dashboardQueryMonolithRestClient) {
        this.restClient = dashboardQueryMonolithRestClient;
    }

    @Override
    public List<PainelConfiguracaoResponse> listarPainels(
            String authorization,
            InternalRequestContext context,
            UUID publicoPainelId,
            String publicoCodigo) {
        try {
            List<PainelConfiguracaoResponse> response = restClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder.path("/api/dashboard/configuracoes/dashboards");
                        if (publicoPainelId != null) {
                            builder.queryParam("publicoPainelId", publicoPainelId);
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
                    .body(new ParameterizedTypeReference<List<PainelConfiguracaoResponse>>() {
                    });
            return response == null ? List.of() : response;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new PainelQueryServiceResourceNotFoundException(exception.getResponseBodyAsString());
            }
            throw exception;
        } catch (ResourceAccessException exception) {
            throw new DownstreamUnavailableException("Monolito indisponivel na leitura de dashboards de configuracao.", exception);
        }
    }
}

