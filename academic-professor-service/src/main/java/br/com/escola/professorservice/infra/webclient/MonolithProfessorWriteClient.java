package br.com.escola.professorservice.infra.webclient;

import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import br.com.escola.professorservice.application.context.InternalHeaders;
import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.dto.ProfessorAllocateRequest;
import br.com.escola.professorservice.application.dto.ProfessorAlocacaoResponse;
import br.com.escola.professorservice.application.dto.ProfessorCreateRequest;
import br.com.escola.professorservice.application.dto.ProfessorResumoResponse;
import br.com.escola.professorservice.application.exception.DownstreamUnavailableException;
import br.com.escola.professorservice.application.port.out.ProfessorCommandWritePort;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class MonolithProfessorWriteClient implements ProfessorCommandWritePort {

    private final RestClient restClient;
    private final MeterRegistry meterRegistry;

    public MonolithProfessorWriteClient(RestClient monolithProfessorRestClient, MeterRegistry meterRegistry) {
        this.restClient = monolithProfessorRestClient;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public ProfessorResumoResponse criarProfessor(
            String authorization,
            InternalRequestContext context,
            ProfessorCreateRequest request) {
        try {
            ProfessorResumoResponse response = restClient.post()
                    .uri("/internal/professores")
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .body(request)
                    .retrieve()
                    .body(ProfessorResumoResponse.class);
            registrarRequisicao("criar", "success");
            return response;
        } catch (RestClientResponseException exception) {
            registrarErro("criar", exception);
            throw exception;
        } catch (ResourceAccessException exception) {
            registrarErro("criar", exception);
            throw new DownstreamUnavailableException("Monolito indisponivel para escrita shadow de professor", exception);
        }
    }

    @Override
    public ProfessorAlocacaoResponse alocarProfessorTurmaDisciplina(
            String authorization,
            InternalRequestContext context,
            UUID professorId,
            ProfessorAllocateRequest request) {
        try {
            ProfessorAlocacaoResponse response = restClient.post()
                    .uri("/internal/professores/{id}/turmas-disciplinas", professorId)
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .body(request)
                    .retrieve()
                    .body(ProfessorAlocacaoResponse.class);
            registrarRequisicao("vincularTurmaDisciplina", "success");
            return response;
        } catch (RestClientResponseException exception) {
            registrarErro("vincularTurmaDisciplina", exception);
            throw exception;
        } catch (ResourceAccessException exception) {
            registrarErro("vincularTurmaDisciplina", exception);
            throw new DownstreamUnavailableException("Monolito indisponivel para escrita shadow de alocacao", exception);
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
