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
import br.com.escola.pedagogicalservice.application.dto.NotaAlunoResponse;
import br.com.escola.pedagogicalservice.application.exception.DownstreamUnavailableException;
import br.com.escola.pedagogicalservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.pedagogicalservice.application.port.out.AvaliacaoPort;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class OrigemAtualAvaliacaoClient implements AvaliacaoPort {

    private final RestClient restClient;
    private final MeterRegistry meterRegistry;

    public OrigemAtualAvaliacaoClient(RestClient monolithPedagogicalRestClient, MeterRegistry meterRegistry) {
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
                throw new RecursoNaoEncontradoException("Dependencia de avaliacao nao encontrada");
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
                throw new RecursoNaoEncontradoException("Consulta de avaliacoes nao encontrada");
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
                throw new RecursoNaoEncontradoException("Avaliacao nao encontrada");
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

    @Override
    public NotaAlunoResponse lancarNota(
            String authorization,
            InternalRequestContext context,
            UUID avaliacaoId,
            String requestBody) {
        try {
            NotaAlunoResponse response = restClient.post()
                    .uri("/internal/avaliacoes/{id}/notas", avaliacaoId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .body(requestBody)
                    .retrieve()
                    .body(NotaAlunoResponse.class);
            meterRegistry.counter("pedagogical.monolith.requests", "route", "avaliacaoLancarNota", "result", "success")
                    .increment();
            return response;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                meterRegistry.counter("pedagogical.monolith.requests", "route", "avaliacaoLancarNota", "result", "not_found")
                        .increment();
                throw new RecursoNaoEncontradoException("Dependencia de nota nao encontrada");
            }
            meterRegistry.counter("pedagogical.monolith.requests", "route", "avaliacaoLancarNota", "result", "http_error")
                    .increment();
            throw exception;
        } catch (ResourceAccessException exception) {
            meterRegistry.counter("pedagogical.monolith.requests", "route", "avaliacaoLancarNota", "result", "unavailable")
                    .increment();
            throw new DownstreamUnavailableException("Monolito indisponivel para lancamento de nota", exception);
        }
    }

    @Override
    public List<NotaAlunoResponse> listarNotasPorAvaliacao(
            String authorization,
            InternalRequestContext context,
            UUID avaliacaoId) {
        try {
            List<NotaAlunoResponse> response = restClient.get()
                    .uri("/internal/avaliacoes/{id}/notas", avaliacaoId)
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<NotaAlunoResponse>>() {
                    });
            meterRegistry.counter("pedagogical.monolith.requests", "route", "avaliacaoListarNotas", "result", "success")
                    .increment();
            return response == null ? List.of() : response;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                meterRegistry.counter("pedagogical.monolith.requests", "route", "avaliacaoListarNotas", "result", "not_found")
                        .increment();
                throw new RecursoNaoEncontradoException("Consulta de notas por avaliacao nao encontrada");
            }
            meterRegistry.counter("pedagogical.monolith.requests", "route", "avaliacaoListarNotas", "result", "http_error")
                    .increment();
            throw exception;
        } catch (ResourceAccessException exception) {
            meterRegistry.counter("pedagogical.monolith.requests", "route", "avaliacaoListarNotas", "result", "unavailable")
                    .increment();
            throw new DownstreamUnavailableException("Monolito indisponivel para listagem de notas por avaliacao", exception);
        }
    }

    @Override
    public List<NotaAlunoResponse> listarNotasPorMatricula(
            String authorization,
            InternalRequestContext context,
            UUID matriculaId) {
        try {
            List<NotaAlunoResponse> response = restClient.get()
                    .uri("/internal/matriculas/{matriculaId}/notas", matriculaId)
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<NotaAlunoResponse>>() {
                    });
            meterRegistry.counter("pedagogical.monolith.requests", "route", "matriculaListarNotas", "result", "success")
                    .increment();
            return response == null ? List.of() : response;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                meterRegistry.counter("pedagogical.monolith.requests", "route", "matriculaListarNotas", "result", "not_found")
                        .increment();
                throw new RecursoNaoEncontradoException("Consulta de notas por matricula nao encontrada");
            }
            meterRegistry.counter("pedagogical.monolith.requests", "route", "matriculaListarNotas", "result", "http_error")
                    .increment();
            throw exception;
        } catch (ResourceAccessException exception) {
            meterRegistry.counter("pedagogical.monolith.requests", "route", "matriculaListarNotas", "result", "unavailable")
                    .increment();
            throw new DownstreamUnavailableException("Monolito indisponivel para listagem de notas por matricula", exception);
        }
    }

    private void enrichHeaders(HttpHeaders headers, String authorization, InternalRequestContext context) {
        headers.set(HttpHeaders.AUTHORIZATION, authorization);
        headers.set(InternalHeaders.CORRELATION_ID, context.correlationId());
        headers.set(InternalHeaders.USUARIO_ID, context.usuarioId().toString());
        headers.set(InternalHeaders.ESCOLA_ID, context.escolaId().toString());
    }
}

