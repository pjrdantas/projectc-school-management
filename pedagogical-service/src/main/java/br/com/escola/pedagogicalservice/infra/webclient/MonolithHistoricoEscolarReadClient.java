package br.com.escola.pedagogicalservice.infra.webclient;

import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.escola.pedagogicalservice.application.context.InternalHeaders;
import br.com.escola.pedagogicalservice.application.context.InternalRequestContext;
import br.com.escola.pedagogicalservice.application.dto.HistoricoEscolarTelaResponse;
import br.com.escola.pedagogicalservice.application.exception.DownstreamUnavailableException;
import br.com.escola.pedagogicalservice.application.exception.PedagogicalServiceResourceNotFoundException;
import br.com.escola.pedagogicalservice.application.port.out.HistoricoEscolarReadPort;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class MonolithHistoricoEscolarReadClient implements HistoricoEscolarReadPort {

    private final RestClient restClient;
    private final MeterRegistry meterRegistry;

    public MonolithHistoricoEscolarReadClient(RestClient monolithPedagogicalRestClient, MeterRegistry meterRegistry) {
        this.restClient = monolithPedagogicalRestClient;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public HistoricoEscolarTelaResponse carregarNovo(
            String authorization,
            InternalRequestContext context,
            UUID alunoId,
            UUID matriculaId,
            String modo) {
        try {
            String uri = UriComponentsBuilder.fromPath("/internal/historicos-escolares/novo")
                    .queryParam("idAluno", alunoId)
                    .queryParam("idMatricula", matriculaId)
                    .queryParam("modo", modo)
                    .build()
                    .toUriString();
            HistoricoEscolarTelaResponse response = restClient.get()
                    .uri(uri)
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .retrieve()
                    .body(HistoricoEscolarTelaResponse.class);
            meterRegistry.counter("pedagogical.monolith.requests", "route", "historicoNovo", "result", "success").increment();
            return response;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                meterRegistry.counter("pedagogical.monolith.requests", "route", "historicoNovo", "result", "not_found").increment();
                throw new PedagogicalServiceResourceNotFoundException("Carregamento de historico escolar nao encontrado");
            }
            meterRegistry.counter("pedagogical.monolith.requests", "route", "historicoNovo", "result", "http_error").increment();
            throw exception;
        } catch (ResourceAccessException exception) {
            meterRegistry.counter("pedagogical.monolith.requests", "route", "historicoNovo", "result", "unavailable").increment();
            throw new DownstreamUnavailableException("Monolito indisponivel para carregamento novo de historico escolar", exception);
        }
    }

    @Override
    public HistoricoEscolarTelaResponse carregarParaEdicao(
            String authorization,
            InternalRequestContext context,
            UUID historicoEscolarId) {
        try {
            HistoricoEscolarTelaResponse response = restClient.get()
                    .uri("/internal/historicos-escolares/{id}/carregamento", historicoEscolarId)
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .retrieve()
                    .body(HistoricoEscolarTelaResponse.class);
            meterRegistry.counter("pedagogical.monolith.requests", "route", "historicoCarregamento", "result", "success").increment();
            return response;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                meterRegistry.counter("pedagogical.monolith.requests", "route", "historicoCarregamento", "result", "not_found")
                        .increment();
                throw new PedagogicalServiceResourceNotFoundException("Historico escolar nao encontrado para carregamento");
            }
            meterRegistry.counter("pedagogical.monolith.requests", "route", "historicoCarregamento", "result", "http_error").increment();
            throw exception;
        } catch (ResourceAccessException exception) {
            meterRegistry.counter("pedagogical.monolith.requests", "route", "historicoCarregamento", "result", "unavailable")
                    .increment();
            throw new DownstreamUnavailableException("Monolito indisponivel para carregamento de historico escolar", exception);
        }
    }

    private void enrichHeaders(HttpHeaders headers, String authorization, InternalRequestContext context) {
        headers.set(HttpHeaders.AUTHORIZATION, authorization);
        headers.set(InternalHeaders.CORRELATION_ID, context.correlationId());
        headers.set(InternalHeaders.USUARIO_ID, context.usuarioId().toString());
        headers.set(InternalHeaders.ESCOLA_ID, context.escolaId().toString());
    }
}
