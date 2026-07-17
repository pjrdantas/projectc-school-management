package br.com.escola.identityaccessservice.infra.webclient;

import java.util.List;
import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import br.com.escola.identityaccessservice.application.context.InternalRequestContext;
import br.com.escola.identityaccessservice.application.dto.AuthContextResponse;
import br.com.escola.identityaccessservice.application.dto.EscolaSessaoResponse;
import br.com.escola.identityaccessservice.application.exception.DownstreamUnavailableException;
import br.com.escola.identityaccessservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.identityaccessservice.application.port.out.SessaoAutenticadaPort;

@Component
public class LegacySessaoAutenticadaClient implements SessaoAutenticadaPort {

    private final RestClient restClient;

    public LegacySessaoAutenticadaClient(RestClient monolithIdentityAccessRestClient) {
        this.restClient = monolithIdentityAccessRestClient;
    }

    @Override
    public List<EscolaSessaoResponse> listarEscolasDisponiveis(
            String authorization,
            InternalRequestContext context) {
        try {
            List<EscolaSessaoResponse> response = restClient.get()
                    .uri("/internal/auth/escolas")
                    .headers(headers -> enrichHeaders(headers, authorization))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<EscolaSessaoResponse>>() {
                    });
            return response == null ? List.of() : response;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new RecursoNaoEncontradoException("Consulta de escolas da sessao nao encontrada");
            }
            throw exception;
        } catch (ResourceAccessException exception) {
            throw new DownstreamUnavailableException("Monolito indisponivel para escolas da sessao", exception);
        }
    }

    @Override
    public AuthContextResponse selecionarEscolaAtiva(
            String authorization,
            InternalRequestContext context,
            UUID escolaId) {
        try {
            AuthContextResponse response = restClient.post()
                    .uri("/internal/auth/escola-ativa")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> enrichHeaders(headers, authorization))
                    .body(new SelecionarEscolaAtivaLegacyRequest(escolaId))
                    .retrieve()
                    .body(AuthContextResponse.class);
            return response;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new RecursoNaoEncontradoException("Selecao de escola ativa nao encontrada");
            }
            throw exception;
        } catch (ResourceAccessException exception) {
            throw new DownstreamUnavailableException("Monolito indisponivel para selecao de escola ativa", exception);
        }
    }

    private void enrichHeaders(HttpHeaders headers, String authorization) {
        headers.set(HttpHeaders.AUTHORIZATION, authorization);
    }

    private record SelecionarEscolaAtivaLegacyRequest(UUID escolaId) {
    }
}

