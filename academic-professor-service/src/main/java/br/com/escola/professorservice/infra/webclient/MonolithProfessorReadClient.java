package br.com.escola.professorservice.infra.webclient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import br.com.escola.professorservice.application.context.InternalHeaders;
import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.dto.FuncionarioElegivelResponse;
import br.com.escola.professorservice.application.dto.ProfessorAlocacaoResponse;
import br.com.escola.professorservice.application.dto.ProfessorResumoResponse;
import br.com.escola.professorservice.application.exception.DownstreamUnavailableException;
import br.com.escola.professorservice.application.port.out.ProfessorReadPort;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class MonolithProfessorReadClient implements ProfessorReadPort {

    private final RestClient restClient;
    private final MeterRegistry meterRegistry;

    public MonolithProfessorReadClient(RestClient monolithProfessorRestClient, MeterRegistry meterRegistry) {
        this.restClient = monolithProfessorRestClient;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public List<ProfessorResumoResponse> listarProfessores(String authorization, InternalRequestContext context) {
        try {
            List<ProfessorResumoResponse> response = restClient.get()
                    .uri("/internal/professores")
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<ProfessorResumoResponse>>() {
                    });
            registrarRequisicao("listar", "success");
            return response == null ? List.of() : response;
        } catch (RestClientResponseException exception) {
            registrarErro("listar", exception);
            throw exception;
        } catch (ResourceAccessException exception) {
            registrarErro("listar", exception);
            throw new DownstreamUnavailableException("Monolito indisponivel para leitura shadow de professores", exception);
        }
    }

    @Override
    public Optional<ProfessorResumoResponse> buscarProfessorPorId(
            String authorization,
            InternalRequestContext context,
            UUID professorId) {
        try {
            ProfessorResumoResponse response = restClient.get()
                    .uri("/internal/professores/{id}", professorId)
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .retrieve()
                    .body(ProfessorResumoResponse.class);
            registrarRequisicao("buscarPorId", "success");
            return Optional.ofNullable(response);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                registrarRequisicao("buscarPorId", "not_found");
                return Optional.empty();
            }
            registrarErro("buscarPorId", exception);
            throw exception;
        } catch (ResourceAccessException exception) {
            registrarErro("buscarPorId", exception);
            throw new DownstreamUnavailableException("Monolito indisponivel para leitura shadow de professor", exception);
        }
    }

    @Override
    public List<ProfessorAlocacaoResponse> listarAlocacoes(
            String authorization,
            InternalRequestContext context,
            UUID professorId) {
        try {
            List<ProfessorAlocacaoResponse> response = restClient.get()
                    .uri("/internal/professores/{id}/turmas-disciplinas", professorId)
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<ProfessorAlocacaoResponse>>() {
                    });
            registrarRequisicao("listarAlocacoes", "success");
            return response == null ? List.of() : response;
        } catch (RestClientResponseException exception) {
            registrarErro("listarAlocacoes", exception);
            throw exception;
        } catch (ResourceAccessException exception) {
            registrarErro("listarAlocacoes", exception);
            throw new DownstreamUnavailableException("Monolito indisponivel para leitura shadow de alocacoes", exception);
        }
    }

    @Override
    public List<ProfessorAlocacaoResponse> listarProfessoresPorTurma(
            String authorization,
            InternalRequestContext context,
            UUID turmaId) {
        try {
            List<ProfessorAlocacaoResponse> response = restClient.get()
                    .uri("/internal/professores/turmas/{turmaId}", turmaId)
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<ProfessorAlocacaoResponse>>() {
                    });
            registrarRequisicao("listarPorTurma", "success");
            return response == null ? List.of() : response;
        } catch (RestClientResponseException exception) {
            registrarErro("listarPorTurma", exception);
            throw exception;
        } catch (ResourceAccessException exception) {
            registrarErro("listarPorTurma", exception);
            throw new DownstreamUnavailableException("Monolito indisponivel para leitura shadow por turma", exception);
        }
    }

    @Override
    public List<FuncionarioElegivelResponse> listarFuncionariosElegiveis(
            String authorization,
            InternalRequestContext context) {
        try {
            List<FuncionarioElegivelResponse> response = restClient.get()
                    .uri("/internal/funcionarios/professor-elegiveis")
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<FuncionarioElegivelResponse>>() {
                    });
            registrarRequisicao("listarFuncionariosElegiveis", "success");
            return response == null ? List.of() : response;
        } catch (RestClientResponseException exception) {
            registrarErro("listarFuncionariosElegiveis", exception);
            throw exception;
        } catch (ResourceAccessException exception) {
            registrarErro("listarFuncionariosElegiveis", exception);
            throw new DownstreamUnavailableException("Monolito indisponivel para leitura shadow de funcionarios", exception);
        }
    }

    private void enrichHeaders(HttpHeaders headers, String authorization, InternalRequestContext context) {
        headers.set(HttpHeaders.AUTHORIZATION, authorization);
        headers.set(InternalHeaders.CORRELATION_ID, context.correlationId());
        headers.set(InternalHeaders.USUARIO_ID, context.usuarioId().toString());
        headers.set(InternalHeaders.ESCOLA_ID, context.escolaId().toString());
    }

    private void registrarRequisicao(String operacao, String resultado) {
        meterRegistry.counter(
                "professor.shadow.monolith.requests",
                "operacao", operacao,
                "destino", "monolith",
                "resultado", resultado)
                .increment();
    }

    private void registrarErro(String operacao, Exception exception) {
        registrarRequisicao(operacao, "error");
        meterRegistry.counter(
                "professor.shadow.monolith.failures",
                "operacao", operacao,
                "causa", exception.getClass().getSimpleName())
                .increment();
    }
}
