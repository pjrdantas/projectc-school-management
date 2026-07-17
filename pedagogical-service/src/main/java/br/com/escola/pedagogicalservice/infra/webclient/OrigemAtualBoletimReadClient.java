package br.com.escola.pedagogicalservice.infra.webclient;

import java.util.List;
import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import br.com.escola.pedagogicalservice.application.context.InternalHeaders;
import br.com.escola.pedagogicalservice.application.context.InternalRequestContext;
import br.com.escola.pedagogicalservice.application.dto.BoletimResponse;
import br.com.escola.pedagogicalservice.application.exception.DownstreamUnavailableException;
import br.com.escola.pedagogicalservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.pedagogicalservice.application.port.out.BoletimReadPort;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class OrigemAtualBoletimReadClient implements BoletimReadPort {

    private final RestClient restClient;
    private final MeterRegistry meterRegistry;

    public OrigemAtualBoletimReadClient(
            RestClient monolithPedagogicalRestClient,
            MeterRegistry meterRegistry) {
        this.restClient = monolithPedagogicalRestClient;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public BoletimResponse consultarBoletimPorMatricula(
            String authorization,
            InternalRequestContext context,
            UUID matriculaId) {
        try {
            BoletimResponse response = restClient.get()
                    .uri("/internal/boletins/matriculas/{matriculaId}", matriculaId)
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .retrieve()
                    .body(BoletimResponse.class);
            meterRegistry.counter("pedagogical.monolith.requests", "route", "boletimPorMatricula", "result", "success").increment();
            return response;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                meterRegistry.counter("pedagogical.monolith.requests", "route", "boletimPorMatricula", "result", "not_found")
                        .increment();
                throw new RecursoNaoEncontradoException("Boletim nao encontrado para a matricula informada");
            }
            meterRegistry.counter("pedagogical.monolith.requests", "route", "boletimPorMatricula", "result", "http_error")
                    .increment();
            throw exception;
        } catch (ResourceAccessException exception) {
            meterRegistry.counter("pedagogical.monolith.requests", "route", "boletimPorMatricula", "result", "unavailable")
                    .increment();
            throw new DownstreamUnavailableException("Monolito indisponivel para leitura de boletim", exception);
        }
    }

    @Override
    public List<BoletimResponse> listarFechamentosPorMatricula(
            String authorization,
            InternalRequestContext context,
            UUID matriculaId) {
        try {
            List<BoletimResponse> response = restClient.get()
                    .uri("/internal/boletins/matriculas/{matriculaId}/fechamentos", matriculaId)
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<BoletimResponse>>() {
                    });
            meterRegistry.counter("pedagogical.monolith.requests", "route", "boletimFechamentosPorMatricula", "result", "success")
                    .increment();
            return response == null ? List.of() : response;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                meterRegistry.counter("pedagogical.monolith.requests", "route", "boletimFechamentosPorMatricula", "result", "not_found")
                        .increment();
                throw new RecursoNaoEncontradoException("Fechamentos de boletim nao encontrados para a matricula informada");
            }
            meterRegistry.counter("pedagogical.monolith.requests", "route", "boletimFechamentosPorMatricula", "result", "http_error")
                    .increment();
            throw exception;
        } catch (ResourceAccessException exception) {
            meterRegistry.counter("pedagogical.monolith.requests", "route", "boletimFechamentosPorMatricula", "result", "unavailable")
                    .increment();
            throw new DownstreamUnavailableException("Monolito indisponivel para leitura de fechamentos de boletim", exception);
        }
    }

    private void enrichHeaders(HttpHeaders headers, String authorization, InternalRequestContext context) {
        headers.set(HttpHeaders.AUTHORIZATION, authorization);
        headers.set(InternalHeaders.CORRELATION_ID, context.correlationId());
        headers.set(InternalHeaders.USUARIO_ID, context.usuarioId().toString());
        headers.set(InternalHeaders.ESCOLA_ID, context.escolaId().toString());
    }
}

