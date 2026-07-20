package br.com.escola.peopleservice.infra.webclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import br.com.escola.peopleservice.application.context.InternalHeaders;
import br.com.escola.peopleservice.application.context.InternalRequestContext;
import br.com.escola.peopleservice.application.exception.DownstreamUnavailableException;
import br.com.escola.peopleservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.peopleservice.application.model.EscolaPessoa;
import br.com.escola.peopleservice.application.port.out.EscolaPessoaPort;
import br.com.escola.peopleservice.infra.config.EscolaPessoaClientProperties;

@Component
public class EscolaPessoaHttpAdapter implements EscolaPessoaPort {

    private final RestClient restClient;
    private final EscolaPessoaClientProperties properties;

    public EscolaPessoaHttpAdapter(
            @Qualifier("escolaPessoaRestClient") RestClient restClient,
            EscolaPessoaClientProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @Override
    public EscolaPessoa buscar(
            UUID escolaId,
            String authorization,
            InternalRequestContext context) {
        try {
            EscolaResponse response = restClient.get()
                    .uri("/internal/v1/escolas/{id}", escolaId)
                    .headers(headers -> adicionarHeaders(headers, authorization, context))
                    .retrieve()
                    .body(EscolaResponse.class);
            if (response == null) {
                throw new DownstreamUnavailableException(
                        "Catalogo institucional retornou resposta vazia", null);
            }
            return new EscolaPessoa(response.id(), response.nome(), response.ativo());
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new RecursoNaoEncontradoException("Escola nao encontrada");
            }
            throw new DownstreamUnavailableException(
                    "Catalogo institucional rejeitou a consulta da escola", exception);
        } catch (ResourceAccessException exception) {
            throw new DownstreamUnavailableException(
                    "Catalogo institucional indisponivel para cadastro de aluno", exception);
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
            UUID id,
            String nome,
            boolean ativo) {
    }
}
