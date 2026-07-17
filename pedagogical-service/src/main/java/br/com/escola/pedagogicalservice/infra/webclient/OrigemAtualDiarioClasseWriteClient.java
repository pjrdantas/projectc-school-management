package br.com.escola.pedagogicalservice.infra.webclient;

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
import br.com.escola.pedagogicalservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.pedagogicalservice.application.port.out.DiarioClasseWritePort;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class OrigemAtualDiarioClasseWriteClient implements DiarioClasseWritePort {

    private final RestClient restClient;
    private final MeterRegistry meterRegistry;

    public OrigemAtualDiarioClasseWriteClient(RestClient monolithPedagogicalRestClient, MeterRegistry meterRegistry) {
        this.restClient = monolithPedagogicalRestClient;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public ResponseEntity<String> salvar(
            String authorization,
            InternalRequestContext context,
            String idDiarioClasse,
            String requestBody) {
        try {
            String response = restClient.put()
                    .uri("/internal/diarios-classe/{idDiarioClasse}", idDiarioClasse)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);
            meterRegistry.counter("pedagogical.monolith.requests", "route", "diarioClasseSalvar", "result", "success").increment();
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                meterRegistry.counter("pedagogical.monolith.requests", "route", "diarioClasseSalvar", "result", "not_found").increment();
                throw new RecursoNaoEncontradoException("Diario de classe nao encontrado para salvamento");
            }
            meterRegistry.counter("pedagogical.monolith.requests", "route", "diarioClasseSalvar", "result", "http_error").increment();
            throw exception;
        } catch (ResourceAccessException exception) {
            meterRegistry.counter("pedagogical.monolith.requests", "route", "diarioClasseSalvar", "result", "unavailable").increment();
            throw new DownstreamUnavailableException("Monolito indisponivel para salvamento de diario de classe", exception);
        }
    }

    private void enrichHeaders(HttpHeaders headers, String authorization, InternalRequestContext context) {
        headers.set(HttpHeaders.AUTHORIZATION, authorization);
        headers.set(InternalHeaders.CORRELATION_ID, context.correlationId());
        headers.set(InternalHeaders.USUARIO_ID, context.usuarioId().toString());
        headers.set(InternalHeaders.ESCOLA_ID, context.escolaId().toString());
    }
}

