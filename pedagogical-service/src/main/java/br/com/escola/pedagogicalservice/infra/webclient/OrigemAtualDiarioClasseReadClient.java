package br.com.escola.pedagogicalservice.infra.webclient;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.escola.pedagogicalservice.application.context.InternalHeaders;
import br.com.escola.pedagogicalservice.application.context.InternalRequestContext;
import br.com.escola.pedagogicalservice.application.exception.DownstreamUnavailableException;
import br.com.escola.pedagogicalservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.pedagogicalservice.application.port.out.DiarioClasseReadPort;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class OrigemAtualDiarioClasseReadClient implements DiarioClasseReadPort {

    private final RestClient restClient;
    private final MeterRegistry meterRegistry;

    public OrigemAtualDiarioClasseReadClient(RestClient monolithPedagogicalRestClient, MeterRegistry meterRegistry) {
        this.restClient = monolithPedagogicalRestClient;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public ResponseEntity<String> carregar(
            String authorization,
            InternalRequestContext context,
            UUID professorId,
            UUID turmaId,
            UUID disciplinaId,
            Integer anoLetivo,
            Integer mes,
            LocalDate dataReferencia) {
        try {
            String uri = UriComponentsBuilder.fromPath("/internal/diarios-classe")
                    .queryParam("idProfessor", professorId)
                    .queryParam("idTurma", turmaId)
                    .queryParam("idDisciplina", disciplinaId)
                    .queryParam("anoLetivo", anoLetivo)
                    .queryParam("mes", mes)
                    .queryParam("dataReferencia", dataReferencia)
                    .build()
                    .toUriString();
            String response = restClient.get()
                    .uri(uri)
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .retrieve()
                    .body(String.class);
            meterRegistry.counter("pedagogical.monolith.requests", "route", "diarioClasseCarregar", "result", "success").increment();
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                meterRegistry.counter("pedagogical.monolith.requests", "route", "diarioClasseCarregar", "result", "not_found").increment();
                throw new RecursoNaoEncontradoException("Diario de classe nao encontrado");
            }
            meterRegistry.counter("pedagogical.monolith.requests", "route", "diarioClasseCarregar", "result", "http_error").increment();
            throw exception;
        } catch (ResourceAccessException exception) {
            meterRegistry.counter("pedagogical.monolith.requests", "route", "diarioClasseCarregar", "result", "unavailable").increment();
            throw new DownstreamUnavailableException("Monolito indisponivel para leitura de diario de classe", exception);
        }
    }

    private void enrichHeaders(HttpHeaders headers, String authorization, InternalRequestContext context) {
        headers.set(HttpHeaders.AUTHORIZATION, authorization);
        headers.set(InternalHeaders.CORRELATION_ID, context.correlationId());
        headers.set(InternalHeaders.USUARIO_ID, context.usuarioId().toString());
        headers.set(InternalHeaders.ESCOLA_ID, context.escolaId().toString());
    }
}

