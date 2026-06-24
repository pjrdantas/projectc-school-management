package br.com.escola.professor.adapter.out.internal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import br.com.escola.professor.adapter.in.web.dto.internal.ProfessorAlocacaoInternalRequest;
import br.com.escola.professor.adapter.in.web.dto.internal.ProfessorAlocacaoInternalResponse;
import br.com.escola.professor.adapter.in.web.dto.internal.ProfessorInternalRequest;
import br.com.escola.professor.adapter.in.web.dto.internal.ProfessorInternalResponse;
import br.com.escola.professor.application.dto.internal.AlocarProfessorTurmaDisciplinaSolicitacao;
import br.com.escola.professor.application.dto.internal.CriarProfessorSolicitacao;
import br.com.escola.professor.application.dto.internal.ProfessorAlocacaoResumo;
import br.com.escola.professor.application.dto.internal.ProfessorResumo;
import br.com.escola.professor.application.port.internal.ProfessorAcademicoPort;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class ProfessorInternalApiClient implements ProfessorAcademicoPort {

    private static final String ESCOLA_HEADER = "X-Escola-Id";
    private static final String CORRELATION_HEADER = "X-Correlation-Id";

    private final RestClient.Builder restClientBuilder;
    private final Environment environment;

    public ProfessorInternalApiClient(
            RestClient.Builder restClientBuilder,
            Environment environment) {
        this.restClientBuilder = restClientBuilder;
        this.environment = environment;
    }

    @Override
    public ProfessorResumo criarProfessor(UUID escolaId, CriarProfessorSolicitacao solicitacao) {
        ProfessorInternalResponse response = restClient().post()
                .uri("/internal/professores")
                .headers(headers -> preencherHeaders(headers, escolaId))
                .body(new ProfessorInternalRequest(
                        solicitacao.funcionarioId(),
                        solicitacao.registroProfissional(),
                        solicitacao.formacao(),
                        solicitacao.ativo()))
                .retrieve()
                .body(ProfessorInternalResponse.class);

        return toProfessorResumo(response);
    }

    @Override
    public List<ProfessorResumo> listarProfessores(UUID escolaId) {
        List<ProfessorInternalResponse> response = restClient().get()
                .uri("/internal/professores")
                .headers(headers -> preencherHeaders(headers, escolaId))
                .retrieve()
                .body(new ParameterizedTypeReference<List<ProfessorInternalResponse>>() {
                });

        if (response == null) {
            return List.of();
        }

        return response.stream()
                .map(this::toProfessorResumo)
                .toList();
    }

    @Override
    public Optional<ProfessorResumo> buscarProfessor(UUID escolaId, UUID professorId) {
        try {
            ProfessorInternalResponse response = restClient().get()
                    .uri("/internal/professores/{id}", professorId)
                    .headers(headers -> preencherHeaders(headers, escolaId))
                    .retrieve()
                    .body(ProfessorInternalResponse.class);
            return Optional.ofNullable(response).map(this::toProfessorResumo);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                return Optional.empty();
            }
            throw exception;
        }
    }

    @Override
    public boolean existeProfessor(UUID escolaId, UUID professorId) {
        return buscarProfessor(escolaId, professorId).isPresent();
    }

    @Override
    public ProfessorAlocacaoResumo alocarProfessorTurmaDisciplina(
            UUID escolaId,
            UUID professorId,
            AlocarProfessorTurmaDisciplinaSolicitacao solicitacao) {
        ProfessorAlocacaoInternalResponse response = restClient().post()
                .uri("/internal/professores/{id}/turmas-disciplinas", professorId)
                .headers(headers -> preencherHeaders(headers, escolaId))
                .body(new ProfessorAlocacaoInternalRequest(
                        solicitacao.turmaDisciplinaId(),
                        solicitacao.dataInicio(),
                        solicitacao.dataFim(),
                        solicitacao.ativo()))
                .retrieve()
                .body(ProfessorAlocacaoInternalResponse.class);

        return toAlocacaoResumo(response);
    }

    @Override
    public List<ProfessorAlocacaoResumo> listarAlocacoes(UUID escolaId, UUID professorId) {
        List<ProfessorAlocacaoInternalResponse> response = restClient().get()
                .uri("/internal/professores/{id}/turmas-disciplinas", professorId)
                .headers(headers -> preencherHeaders(headers, escolaId))
                .retrieve()
                .body(new ParameterizedTypeReference<List<ProfessorAlocacaoInternalResponse>>() {
                });

        if (response == null) {
            return List.of();
        }

        return response.stream()
                .map(this::toAlocacaoResumo)
                .toList();
    }

    @Override
    public List<ProfessorAlocacaoResumo> listarProfessoresPorTurma(UUID escolaId, UUID turmaId) {
        List<ProfessorAlocacaoInternalResponse> response = restClient().get()
                .uri("/internal/professores/turmas/{turmaId}", turmaId)
                .headers(headers -> preencherHeaders(headers, escolaId))
                .retrieve()
                .body(new ParameterizedTypeReference<List<ProfessorAlocacaoInternalResponse>>() {
                });

        if (response == null) {
            return List.of();
        }

        return response.stream()
                .map(this::toAlocacaoResumo)
                .toList();
    }

    private void preencherHeaders(HttpHeaders headers, UUID escolaId) {
        headers.add(ESCOLA_HEADER, escolaId.toString());
        HttpServletRequest request = requestAtual();
        if (request == null) {
            return;
        }

        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && !authorization.isBlank()) {
            headers.add(HttpHeaders.AUTHORIZATION, authorization);
        }

        String correlationId = request.getHeader(CORRELATION_HEADER);
        if (correlationId != null && !correlationId.isBlank()) {
            headers.add(CORRELATION_HEADER, correlationId);
        }
    }

    private HttpServletRequest requestAtual() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return servletRequestAttributes.getRequest();
        }
        return null;
    }

    private RestClient restClient() {
        String baseUrl = environment.resolvePlaceholders(
                environment.getProperty("professor.internal-client.base-url", "http://localhost:8080"));
        return restClientBuilder.baseUrl(baseUrl).build();
    }

    private ProfessorResumo toProfessorResumo(ProfessorInternalResponse response) {
        if (response == null) {
            return null;
        }
        return new ProfessorResumo(
                response.id(),
                response.pessoaId(),
                response.nomeCompleto(),
                response.escolaId(),
                response.escolaNome(),
                response.registroProfissional(),
                response.formacao(),
                response.ativo(),
                response.createdAt(),
                response.updatedAt());
    }

    private ProfessorAlocacaoResumo toAlocacaoResumo(ProfessorAlocacaoInternalResponse response) {
        if (response == null) {
            return null;
        }
        return new ProfessorAlocacaoResumo(
                response.id(),
                response.professorId(),
                response.professorNome(),
                response.turmaDisciplinaId(),
                response.turmaId(),
                response.turmaNome(),
                response.disciplinaId(),
                response.disciplinaNome(),
                response.dataInicio(),
                response.dataFim(),
                response.ativo(),
                response.createdAt());
    }
}
