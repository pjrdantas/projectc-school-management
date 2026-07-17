package br.com.escola.professorservice.infra.webclient;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import br.com.escola.professorservice.application.context.InternalHeaders;
import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.dto.FuncionarioElegivelResponse;
import br.com.escola.professorservice.application.exception.DownstreamUnavailableException;
import br.com.escola.professorservice.application.port.out.FuncionarioElegivelPort;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class LegacyFuncionarioElegivelClient implements FuncionarioElegivelPort {

    private final RestClient restClient;
    private final MeterRegistry meterRegistry;

    public LegacyFuncionarioElegivelClient(RestClient monolithProfessorRestClient, MeterRegistry meterRegistry) {
        this.restClient = monolithProfessorRestClient;
        this.meterRegistry = meterRegistry;
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
