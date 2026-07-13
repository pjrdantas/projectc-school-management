package br.com.escola.planningaiservice.infra.webclient;

import java.util.List;
import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriBuilder;

import br.com.escola.planningaiservice.application.context.InternalRequestContext;
import br.com.escola.planningaiservice.application.dto.BibliotecaConteudoPedagogicoResponse;
import br.com.escola.planningaiservice.application.exception.DownstreamUnavailableException;
import br.com.escola.planningaiservice.application.exception.PlanningAiServiceResourceNotFoundException;
import br.com.escola.planningaiservice.application.port.out.PlanningAiReadPort;

@Component
public class MonolithPlanningAiReadClient implements PlanningAiReadPort {

    private final RestClient restClient;

    public MonolithPlanningAiReadClient(RestClient monolithPlanningAiRestClient) {
        this.restClient = monolithPlanningAiRestClient;
    }

    @Override
    public List<BibliotecaConteudoPedagogicoResponse> listarBiblioteca(
            String authorization,
            InternalRequestContext context,
            UUID professorId,
            UUID disciplinaId,
            String tipoConteudo,
            String tema) {
        try {
            List<BibliotecaConteudoPedagogicoResponse> response = restClient.get()
                    .uri(uriBuilder -> bibliotecaUri(uriBuilder, professorId, disciplinaId, tipoConteudo, tema))
                    .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, authorization))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<BibliotecaConteudoPedagogicoResponse>>() {
                    });
            return response == null ? List.of() : response;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new PlanningAiServiceResourceNotFoundException(
                        "Consulta da biblioteca pedagogica nao encontrada");
            }
            throw exception;
        } catch (ResourceAccessException exception) {
            throw new DownstreamUnavailableException(
                    "Monolito indisponivel para consulta da biblioteca pedagogica",
                    exception);
        }
    }

    private java.net.URI bibliotecaUri(
            UriBuilder uriBuilder,
            UUID professorId,
            UUID disciplinaId,
            String tipoConteudo,
            String tema) {
        UriBuilder builder = uriBuilder.path("/api/biblioteca-conteudos-pedagogicos");
        if (professorId != null) {
            builder.queryParam("professorId", professorId);
        }
        if (disciplinaId != null) {
            builder.queryParam("disciplinaId", disciplinaId);
        }
        if (tipoConteudo != null && !tipoConteudo.isBlank()) {
            builder.queryParam("tipoConteudo", tipoConteudo);
        }
        if (tema != null && !tema.isBlank()) {
            builder.queryParam("tema", tema);
        }
        return builder.build();
    }
}
