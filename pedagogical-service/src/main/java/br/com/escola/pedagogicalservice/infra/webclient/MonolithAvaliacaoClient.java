package br.com.escola.pedagogicalservice.infra.webclient;

import java.util.List;
import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.escola.pedagogicalservice.application.context.InternalHeaders;
import br.com.escola.pedagogicalservice.application.context.InternalRequestContext;
import br.com.escola.pedagogicalservice.application.dto.AvaliacaoResponse;
import br.com.escola.pedagogicalservice.application.exception.DownstreamUnavailableException;
import br.com.escola.pedagogicalservice.application.exception.PedagogicalServiceResourceNotFoundException;
import br.com.escola.pedagogicalservice.application.port.out.AvaliacaoPort;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class MonolithAvaliacaoClient implements AvaliacaoPort {

    private final RestClient restClient;
    private final MeterRegistry meterRegistry;

    public MonolithAvaliacaoClient(RestClient monolithPedagogicalRestClient, MeterRegistry meterRegistry) {
        this.restClient = monolithPedagogicalRestClient;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public AvaliacaoResponse criar(String authorization, InternalRequestContext context, String requestBody) {
        try {
            AvaliacaoResponse response = restClient.post()
                    .uri("/internal/avaliacoes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .body(requestBody)
                    .retrieve()
                    .body(AvaliacaoResponse.class);
            meterRegistry.counter("pedagogical.monolith.requests", "route", "avaliacaoCriar", "result", "success")
                    .increment();
            return response;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                meterRegistry.counter("pedagogical.monolith.requests", "route", "avaliacaoCriar", "result", "not_found")
                        .increment();
                throw new PedagogicalServiceResourceNotFoundException("Dependencia de avaliacao nao encontrada");
            }
            meterRegistry.counter("pedagogical.monolith.requests", "route", "avaliacaoCriar", "result", "http_error")
                    .increment();
            throw exception;
        } catch (ResourceAccessException exception) {
            meterRegistry.counter("pedagogical.monolith.requests", "route", "avaliacaoCriar", "result", "unavailable")
                    .increment();
            throw new DownstreamUnavailableException("Monolito indisponivel para criacao de avaliacao", exception);
        }
    }

    @Override
    public List<AvaliacaoResponse> listar(
            String authorization,
            InternalRequestContext context,
            UUID professorTurmaDisciplinaId,
            UUID turmaId) {
        try {
            String uri = UriComponentsBuilder.fromPath("/internal/avaliacoes")
                    .queryParamIfPresent("professorTurmaDisciplinaId", java.util.Optional.ofNullable(professorTurmaDisciplinaId))
                    .queryParamIfPresent("turmaId", java.util.Optional.ofNullable(turmaId))
                    .build()
                    .toUriString();
            List<AvaliacaoResponse> response = restClient.get()
                    .uri(uri)
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<AvaliacaoResponse>>() {
                    });
            meterRegistry.counter("pedagogical.monolith.requests", "route", "avaliacaoListar", "result", "success")
                    .increment();
            return response == null ? List.of() : response;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                meterRegistry.counter("pedagogical.monolith.requests", "route", "avaliacaoListar", "result", "not_found")
                        .increment();
                throw new PedagogicalServiceResourceNotFoundException("Consulta de avaliacoes nao encontrada");
            }
            meterRegistry.counter("pedagogical.monolith.requests", "route", "avaliacaoListar", "result", "http_error")
                    .increment();
            throw exception;
        } catch (ResourceAccessException exception) {
            meterRegistry.counter("pedagogical.monolith.requests", "route", "avaliacaoListar", "result", "unavailable")
                    .increment();
            throw new DownstreamUnavailableException("Monolito indisponivel para listagem de avaliacoes", exception);
        }
    }

    @Override
    public AvaliacaoResponse buscarPorId(String authorization, InternalRequestContext context, UUID avaliacaoId) {
        try {
            AvaliacaoResponse response = restClient.get()
                    .uri("/internal/avaliacoes/{id}", avaliacaoId)
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .retrieve()
                    .body(AvaliacaoResponse.class);
            meterRegistry.counter("pedagogical.monolith.requests", "route", "avaliacaoBuscarPorId", "result", "success")
                    .increment();
            return response;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                meterRegistry.counter("pedagogical.monolith.requests", "route", "avaliacaoBuscarPorId", "result", "not_found")
                        .increment();
                throw new PedagogicalServiceResourceNotFoundException("Avaliacao nao encontrada");
            }
            meterRegistry.counter("pedagogical.monolith.requests", "route", "avaliacaoBuscarPorId", "result", "http_error")
                    .increment();
            throw exception;
        } catch (ResourceAccessException exception) {
            meterRegistry.counter("pedagogical.monolith.requests", "route", "avaliacaoBuscarPorId", "result", "unavailable")
                    .increment();
            throw new DownstreamUnavailableException("Monolito indisponivel para consulta de avaliacao", exception);
        }
    }

    private void enrichHeaders(HttpHeaders headers, String authorization, InternalRequestContext context) {
        headers.set(HttpHeaders.AUTHORIZATION, authorization);
        headers.set(InternalHeaders.CORRELATION_ID, context.correlationId());
        headers.set(InternalHeaders.USUARIO_ID, context.usuarioId().toString());
        headers.set(InternalHeaders.ESCOLA_ID, context.escolaId().toString());
    }
}
