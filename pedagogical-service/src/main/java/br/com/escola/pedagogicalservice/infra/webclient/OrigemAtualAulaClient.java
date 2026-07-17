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
import br.com.escola.pedagogicalservice.application.dto.AulaResponse;
import br.com.escola.pedagogicalservice.application.dto.FrequenciaAlunoResponse;
import br.com.escola.pedagogicalservice.application.dto.FrequenciaDocenteResponse;
import br.com.escola.pedagogicalservice.application.exception.DownstreamUnavailableException;
import br.com.escola.pedagogicalservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.pedagogicalservice.application.port.out.AulaPort;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class OrigemAtualAulaClient implements AulaPort {

    private final RestClient restClient;
    private final MeterRegistry meterRegistry;

    public OrigemAtualAulaClient(RestClient monolithPedagogicalRestClient, MeterRegistry meterRegistry) {
        this.restClient = monolithPedagogicalRestClient;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public AulaResponse criar(String authorization, InternalRequestContext context, String requestBody) {
        try {
            AulaResponse response = restClient.post()
                    .uri("/internal/aulas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .body(requestBody)
                    .retrieve()
                    .body(AulaResponse.class);
            meterRegistry.counter("pedagogical.monolith.requests", "route", "aulaCriar", "result", "success").increment();
            return response;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                meterRegistry.counter("pedagogical.monolith.requests", "route", "aulaCriar", "result", "not_found").increment();
                throw new RecursoNaoEncontradoException("Dependencia de aula nao encontrada");
            }
            meterRegistry.counter("pedagogical.monolith.requests", "route", "aulaCriar", "result", "http_error").increment();
            throw exception;
        } catch (ResourceAccessException exception) {
            meterRegistry.counter("pedagogical.monolith.requests", "route", "aulaCriar", "result", "unavailable").increment();
            throw new DownstreamUnavailableException("Monolito indisponivel para criacao de aula", exception);
        }
    }

    @Override
    public List<AulaResponse> listar(
            String authorization,
            InternalRequestContext context,
            UUID professorTurmaDisciplinaId,
            UUID turmaId) {
        try {
            String uri = UriComponentsBuilder.fromPath("/internal/aulas")
                    .queryParamIfPresent("professorTurmaDisciplinaId", java.util.Optional.ofNullable(professorTurmaDisciplinaId))
                    .queryParamIfPresent("turmaId", java.util.Optional.ofNullable(turmaId))
                    .build()
                    .toUriString();
            List<AulaResponse> response = restClient.get()
                    .uri(uri)
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<AulaResponse>>() {
                    });
            meterRegistry.counter("pedagogical.monolith.requests", "route", "aulaListar", "result", "success").increment();
            return response == null ? List.of() : response;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                meterRegistry.counter("pedagogical.monolith.requests", "route", "aulaListar", "result", "not_found").increment();
                throw new RecursoNaoEncontradoException("Consulta de aulas nao encontrada");
            }
            meterRegistry.counter("pedagogical.monolith.requests", "route", "aulaListar", "result", "http_error").increment();
            throw exception;
        } catch (ResourceAccessException exception) {
            meterRegistry.counter("pedagogical.monolith.requests", "route", "aulaListar", "result", "unavailable").increment();
            throw new DownstreamUnavailableException("Monolito indisponivel para listagem de aulas", exception);
        }
    }

    @Override
    public AulaResponse buscarPorId(String authorization, InternalRequestContext context, UUID aulaId) {
        try {
            AulaResponse response = restClient.get()
                    .uri("/internal/aulas/{id}", aulaId)
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .retrieve()
                    .body(AulaResponse.class);
            meterRegistry.counter("pedagogical.monolith.requests", "route", "aulaBuscarPorId", "result", "success").increment();
            return response;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                meterRegistry.counter("pedagogical.monolith.requests", "route", "aulaBuscarPorId", "result", "not_found").increment();
                throw new RecursoNaoEncontradoException("Aula nao encontrada");
            }
            meterRegistry.counter("pedagogical.monolith.requests", "route", "aulaBuscarPorId", "result", "http_error").increment();
            throw exception;
        } catch (ResourceAccessException exception) {
            meterRegistry.counter("pedagogical.monolith.requests", "route", "aulaBuscarPorId", "result", "unavailable").increment();
            throw new DownstreamUnavailableException("Monolito indisponivel para consulta de aula", exception);
        }
    }

    @Override
    public FrequenciaDocenteResponse registrarFrequenciaProfessor(
            String authorization,
            InternalRequestContext context,
            UUID aulaId,
            String requestBody) {
        try {
            FrequenciaDocenteResponse response = restClient.post()
                    .uri("/internal/aulas/{id}/frequencia-professor", aulaId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .body(requestBody)
                    .retrieve()
                    .body(FrequenciaDocenteResponse.class);
            meterRegistry.counter("pedagogical.monolith.requests", "route", "aulaFrequenciaProfessorCriar", "result", "success").increment();
            return response;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                meterRegistry.counter("pedagogical.monolith.requests", "route", "aulaFrequenciaProfessorCriar", "result", "not_found").increment();
                throw new RecursoNaoEncontradoException("Dependencia de frequencia de professor nao encontrada");
            }
            meterRegistry.counter("pedagogical.monolith.requests", "route", "aulaFrequenciaProfessorCriar", "result", "http_error").increment();
            throw exception;
        } catch (ResourceAccessException exception) {
            meterRegistry.counter("pedagogical.monolith.requests", "route", "aulaFrequenciaProfessorCriar", "result", "unavailable").increment();
            throw new DownstreamUnavailableException("Monolito indisponivel para frequencia de professor", exception);
        }
    }

    @Override
    public List<FrequenciaDocenteResponse> listarFrequenciaProfessor(
            String authorization,
            InternalRequestContext context,
            UUID aulaId) {
        try {
            List<FrequenciaDocenteResponse> response = restClient.get()
                    .uri("/internal/aulas/{id}/frequencia-professor", aulaId)
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<FrequenciaDocenteResponse>>() {
                    });
            meterRegistry.counter("pedagogical.monolith.requests", "route", "aulaFrequenciaProfessorListar", "result", "success").increment();
            return response == null ? List.of() : response;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                meterRegistry.counter("pedagogical.monolith.requests", "route", "aulaFrequenciaProfessorListar", "result", "not_found").increment();
                throw new RecursoNaoEncontradoException("Consulta de frequencia de professor nao encontrada");
            }
            meterRegistry.counter("pedagogical.monolith.requests", "route", "aulaFrequenciaProfessorListar", "result", "http_error").increment();
            throw exception;
        } catch (ResourceAccessException exception) {
            meterRegistry.counter("pedagogical.monolith.requests", "route", "aulaFrequenciaProfessorListar", "result", "unavailable").increment();
            throw new DownstreamUnavailableException("Monolito indisponivel para consulta de frequencia de professor", exception);
        }
    }

    @Override
    public FrequenciaAlunoResponse registrarFrequenciaAluno(
            String authorization,
            InternalRequestContext context,
            UUID aulaId,
            String requestBody) {
        try {
            FrequenciaAlunoResponse response = restClient.post()
                    .uri("/internal/aulas/{id}/frequencias-alunos", aulaId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .body(requestBody)
                    .retrieve()
                    .body(FrequenciaAlunoResponse.class);
            meterRegistry.counter("pedagogical.monolith.requests", "route", "aulaFrequenciaAlunoCriar", "result", "success").increment();
            return response;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                meterRegistry.counter("pedagogical.monolith.requests", "route", "aulaFrequenciaAlunoCriar", "result", "not_found").increment();
                throw new RecursoNaoEncontradoException("Dependencia de frequencia de aluno nao encontrada");
            }
            meterRegistry.counter("pedagogical.monolith.requests", "route", "aulaFrequenciaAlunoCriar", "result", "http_error").increment();
            throw exception;
        } catch (ResourceAccessException exception) {
            meterRegistry.counter("pedagogical.monolith.requests", "route", "aulaFrequenciaAlunoCriar", "result", "unavailable").increment();
            throw new DownstreamUnavailableException("Monolito indisponivel para frequencia de aluno", exception);
        }
    }

    @Override
    public List<FrequenciaAlunoResponse> listarFrequenciasAlunos(
            String authorization,
            InternalRequestContext context,
            UUID aulaId) {
        try {
            List<FrequenciaAlunoResponse> response = restClient.get()
                    .uri("/internal/aulas/{id}/frequencias-alunos", aulaId)
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<FrequenciaAlunoResponse>>() {
                    });
            meterRegistry.counter("pedagogical.monolith.requests", "route", "aulaFrequenciaAlunoListar", "result", "success").increment();
            return response == null ? List.of() : response;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                meterRegistry.counter("pedagogical.monolith.requests", "route", "aulaFrequenciaAlunoListar", "result", "not_found").increment();
                throw new RecursoNaoEncontradoException("Consulta de frequencia de aluno nao encontrada");
            }
            meterRegistry.counter("pedagogical.monolith.requests", "route", "aulaFrequenciaAlunoListar", "result", "http_error").increment();
            throw exception;
        } catch (ResourceAccessException exception) {
            meterRegistry.counter("pedagogical.monolith.requests", "route", "aulaFrequenciaAlunoListar", "result", "unavailable").increment();
            throw new DownstreamUnavailableException("Monolito indisponivel para consulta de frequencia de aluno", exception);
        }
    }

    private void enrichHeaders(HttpHeaders headers, String authorization, InternalRequestContext context) {
        headers.set(HttpHeaders.AUTHORIZATION, authorization);
        headers.set(InternalHeaders.CORRELATION_ID, context.correlationId());
        headers.set(InternalHeaders.USUARIO_ID, context.usuarioId().toString());
        headers.set(InternalHeaders.ESCOLA_ID, context.escolaId().toString());
    }
}

