package br.com.escola.identityaccessservice.infra.webclient;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import br.com.escola.identityaccessservice.application.context.InternalHeaders;
import br.com.escola.identityaccessservice.application.context.InternalRequestContext;
import br.com.escola.identityaccessservice.application.exception.DownstreamUnavailableException;
import br.com.escola.identityaccessservice.application.model.EscolaDisponivel;
import br.com.escola.identityaccessservice.application.port.out.EscolaSessaoPort;
import br.com.escola.identityaccessservice.infra.config.EscolaSessaoClientProperties;

@Component
public class EscolaSessaoHttpAdapter implements EscolaSessaoPort {

    private final RestClient restClient;
    private final EscolaSessaoClientProperties properties;

    public EscolaSessaoHttpAdapter(
            @Qualifier("escolaSessaoRestClient") RestClient restClient,
            EscolaSessaoClientProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @Override
    public List<EscolaDisponivel> listarDisponiveis(
            String authorization,
            InternalRequestContext context) {
        try {
            List<EscolaResponse> response = restClient.get()
                    .uri("/internal/v1/tenant/escolas")
                    .headers(headers -> adicionarHeaders(headers, authorization, context))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<EscolaResponse>>() {
                    });
            return response == null
                    ? List.of()
                    : response.stream().map(EscolaResponse::toModel).toList();
        } catch (RestClientResponseException exception) {
            throw new DownstreamUnavailableException(
                    "Catalogo de escolas rejeitou a consulta da sessao", exception);
        } catch (ResourceAccessException exception) {
            throw new DownstreamUnavailableException(
                    "Catalogo de escolas indisponivel para a sessao", exception);
        }
    }

    private void adicionarHeaders(
            HttpHeaders headers,
            String authorization,
            InternalRequestContext context) {
        headers.set(HttpHeaders.AUTHORIZATION, authorization);
        headers.set(InternalHeaders.INTERNAL_TOKEN, properties.internalToken());
        headers.set(InternalHeaders.CORRELATION_ID, context.correlationId());
        headers.set(InternalHeaders.USUARIO_ID, context.usuarioId().toString());
        headers.set(InternalHeaders.ESCOLA_ID, context.escolaId().toString());
    }

    private record EscolaResponse(
            UUID escolaId,
            String escolaNome,
            boolean ativa) {

        EscolaDisponivel toModel() {
            return new EscolaDisponivel(escolaId, escolaNome, ativa);
        }
    }
}
