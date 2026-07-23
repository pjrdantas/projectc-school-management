package br.com.escola.peopleservice.infra.webclient;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import br.com.escola.peopleservice.application.context.InternalHeaders;
import br.com.escola.peopleservice.application.context.InternalRequestContext;
import br.com.escola.peopleservice.application.dto.PessoaResponsavelVinculadoResponse;
import br.com.escola.peopleservice.application.exception.DownstreamUnavailableException;
import br.com.escola.peopleservice.application.port.out.ResponsaveisAlunoConsultaPort;
import br.com.escola.peopleservice.infra.config.ResponsaveisAlunoClientProperties;

@Component
public class ResponsaveisAlunoHttpAdapter implements ResponsaveisAlunoConsultaPort {

    private static final ParameterizedTypeReference<List<PessoaResponsavelVinculadoResponse>> RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;
    private final ResponsaveisAlunoClientProperties properties;

    public ResponsaveisAlunoHttpAdapter(
            @Qualifier("responsaveisAlunoRestClient") RestClient restClient,
            ResponsaveisAlunoClientProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @Override
    public List<PessoaResponsavelVinculadoResponse> listarResponsaveisPorAluno(
            UUID alunoId,
            String authorization,
            InternalRequestContext context) {
        try {
            List<PessoaResponsavelVinculadoResponse> response = restClient.get()
                    .uri("/internal/v1/alunos/{alunoId}/responsaveis", alunoId)
                    .headers(headers -> adicionarHeaders(headers, authorization, context))
                    .retrieve()
                    .body(RESPONSE_TYPE);
            return response == null ? List.of() : List.copyOf(response);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                return List.of();
            }
            throw new DownstreamUnavailableException(
                    "Servico de responsaveis rejeitou a consulta da ficha do aluno", exception);
        } catch (ResourceAccessException exception) {
            throw new DownstreamUnavailableException(
                    "Servico de responsaveis indisponivel para ficha do aluno", exception);
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
}
