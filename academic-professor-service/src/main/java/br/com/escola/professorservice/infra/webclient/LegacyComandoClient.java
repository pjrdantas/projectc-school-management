package br.com.escola.professorservice.infra.webclient;

import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import br.com.escola.professorservice.application.context.InternalHeaders;
import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.dto.AllocateRequest;
import br.com.escola.professorservice.application.dto.AlocacaoResponse;
import br.com.escola.professorservice.application.dto.CreateRequest;
import br.com.escola.professorservice.application.dto.ResumoResponse;
import br.com.escola.professorservice.application.exception.DownstreamUnavailableException;
import br.com.escola.professorservice.application.port.out.ComandoEscritaPort;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class LegacyComandoClient implements ComandoEscritaPort {

    private final RestClient restClient;
    private final MeterRegistry meterRegistry;

    public LegacyComandoClient(RestClient monolithProfessorRestClient, MeterRegistry meterRegistry) {
        this.restClient = monolithProfessorRestClient;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public ResumoResponse criarProfessor(
            String authorization,
            InternalRequestContext context,
            CreateRequest request) {
        try {
            ResumoResponse response = restClient.post()
                    .uri("/internal/professores")
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .body(request)
                    .retrieve()
                    .body(ResumoResponse.class);
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
    public AlocacaoResponse alocarProfessorTurmaDisciplina(
            String authorization,
            InternalRequestContext context,
            UUID professorId,
            AllocateRequest request) {
        try {
            AlocacaoResponse response = restClient.post()
                    .uri("/internal/professores/{id}/turmas-disciplinas", professorId)
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .body(request)
                    .retrieve()
                    .body(AlocacaoResponse.class);
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

