package br.com.escola.pedagogicalservice.infra.webclient;

import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import br.com.escola.pedagogicalservice.application.context.InternalHeaders;
import br.com.escola.pedagogicalservice.application.context.InternalRequestContext;
import br.com.escola.pedagogicalservice.application.exception.DownstreamUnavailableException;
import br.com.escola.pedagogicalservice.application.exception.PedagogicalServiceResourceNotFoundException;
import br.com.escola.pedagogicalservice.application.port.out.HistoricoEscolarWritePort;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class MonolithHistoricoEscolarWriteClient implements HistoricoEscolarWritePort {

    private final RestClient restClient;
    private final MeterRegistry meterRegistry;

    public MonolithHistoricoEscolarWriteClient(RestClient monolithPedagogicalRestClient, MeterRegistry meterRegistry) {
        this.restClient = monolithPedagogicalRestClient;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public ResponseEntity<String> criar(String authorization, InternalRequestContext context, String requestBody) {
        try {
            String response = restClient.post()
                    .uri("/internal/historicos-escolares")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);
            meterRegistry.counter("pedagogical.monolith.requests", "route", "historicoCriar", "result", "success").increment();
            return ResponseEntity.status(201).contentType(MediaType.APPLICATION_JSON).body(response);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                meterRegistry.counter("pedagogical.monolith.requests", "route", "historicoCriar", "result", "not_found").increment();
                throw new PedagogicalServiceResourceNotFoundException("Dependencia do historico escolar nao encontrada");
            }
            meterRegistry.counter("pedagogical.monolith.requests", "route", "historicoCriar", "result", "http_error").increment();
            throw exception;
        } catch (ResourceAccessException exception) {
            meterRegistry.counter("pedagogical.monolith.requests", "route", "historicoCriar", "result", "unavailable").increment();
            throw new DownstreamUnavailableException("Monolito indisponivel para criacao de historico escolar", exception);
        }
    }

    @Override
    public ResponseEntity<String> atualizar(
            String authorization,
            InternalRequestContext context,
            UUID historicoEscolarId,
            String requestBody) {
        try {
            String response = restClient.put()
                    .uri("/internal/historicos-escolares/{id}", historicoEscolarId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);
            meterRegistry.counter("pedagogical.monolith.requests", "route", "historicoAtualizar", "result", "success").increment();
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                meterRegistry.counter("pedagogical.monolith.requests", "route", "historicoAtualizar", "result", "not_found")
                        .increment();
                throw new PedagogicalServiceResourceNotFoundException("Historico escolar nao encontrado para atualizacao");
            }
            meterRegistry.counter("pedagogical.monolith.requests", "route", "historicoAtualizar", "result", "http_error").increment();
            throw exception;
        } catch (ResourceAccessException exception) {
            meterRegistry.counter("pedagogical.monolith.requests", "route", "historicoAtualizar", "result", "unavailable").increment();
            throw new DownstreamUnavailableException("Monolito indisponivel para atualizacao de historico escolar", exception);
        }
    }

    private void enrichHeaders(HttpHeaders headers, String authorization, InternalRequestContext context) {
        headers.set(HttpHeaders.AUTHORIZATION, authorization);
        headers.set(InternalHeaders.CORRELATION_ID, context.correlationId());
        headers.set(InternalHeaders.USUARIO_ID, context.usuarioId().toString());
        headers.set(InternalHeaders.ESCOLA_ID, context.escolaId().toString());
    }
}
