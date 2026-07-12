package br.com.escola.enrollmentdocumentservice.infra.webclient;

import java.util.List;
import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import br.com.escola.enrollmentdocumentservice.application.context.InternalHeaders;
import br.com.escola.enrollmentdocumentservice.application.context.InternalRequestContext;
import br.com.escola.enrollmentdocumentservice.application.dto.EscolaOrigemRequest;
import br.com.escola.enrollmentdocumentservice.application.dto.EscolaOrigemResponse;
import br.com.escola.enrollmentdocumentservice.application.dto.TransferenciaAlunoRequest;
import br.com.escola.enrollmentdocumentservice.application.dto.TransferenciaAlunoResponse;
import br.com.escola.enrollmentdocumentservice.application.exception.DownstreamUnavailableException;
import br.com.escola.enrollmentdocumentservice.application.exception.EnrollmentDocumentServiceResourceNotFoundException;
import br.com.escola.enrollmentdocumentservice.application.port.out.EnrollmentTransferPort;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class MonolithEnrollmentTransferClient implements EnrollmentTransferPort {

    private final RestClient restClient;
    private final MeterRegistry meterRegistry;

    public MonolithEnrollmentTransferClient(
            RestClient monolithEnrollmentDocumentRestClient,
            MeterRegistry meterRegistry) {
        this.restClient = monolithEnrollmentDocumentRestClient;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public EscolaOrigemResponse criarEscolaOrigem(
            String authorization,
            InternalRequestContext context,
            EscolaOrigemRequest request) {
        try {
            EscolaOrigemResponse response = restClient.post()
                    .uri("/internal/escolas-origem")
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .body(request)
                    .retrieve()
                    .body(EscolaOrigemResponse.class);
            registrarRequisicao("criarEscolaOrigem", "success");
            return response;
        } catch (RestClientResponseException exception) {
            registrarErro("criarEscolaOrigem", exception);
            throw exception;
        } catch (ResourceAccessException exception) {
            registrarErro("criarEscolaOrigem", exception);
            throw new DownstreamUnavailableException("Monolito indisponivel para escrita de escola de origem", exception);
        }
    }

    @Override
    public List<EscolaOrigemResponse> listarEscolasOrigem(String authorization, InternalRequestContext context) {
        try {
            List<EscolaOrigemResponse> response = restClient.get()
                    .uri("/internal/escolas-origem")
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<EscolaOrigemResponse>>() {
                    });
            registrarRequisicao("listarEscolasOrigem", "success");
            return response == null ? List.of() : response;
        } catch (RestClientResponseException exception) {
            registrarErro("listarEscolasOrigem", exception);
            throw exception;
        } catch (ResourceAccessException exception) {
            registrarErro("listarEscolasOrigem", exception);
            throw new DownstreamUnavailableException("Monolito indisponivel para leitura de escolas de origem", exception);
        }
    }

    @Override
    public EscolaOrigemResponse buscarEscolaOrigem(String authorization, InternalRequestContext context, UUID id) {
        try {
            EscolaOrigemResponse response = restClient.get()
                    .uri("/internal/escolas-origem/{id}", id)
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .retrieve()
                    .body(EscolaOrigemResponse.class);
            registrarRequisicao("buscarEscolaOrigem", "success");
            return response;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                registrarRequisicao("buscarEscolaOrigem", "not_found");
                throw new EnrollmentDocumentServiceResourceNotFoundException("Escola de origem nao encontrada");
            }
            registrarErro("buscarEscolaOrigem", exception);
            throw exception;
        } catch (ResourceAccessException exception) {
            registrarErro("buscarEscolaOrigem", exception);
            throw new DownstreamUnavailableException("Monolito indisponivel para leitura de escola de origem", exception);
        }
    }

    @Override
    public TransferenciaAlunoResponse criarTransferencia(
            String authorization,
            InternalRequestContext context,
            TransferenciaAlunoRequest request) {
        try {
            TransferenciaAlunoResponse response = restClient.post()
                    .uri("/internal/transferencias")
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .body(request)
                    .retrieve()
                    .body(TransferenciaAlunoResponse.class);
            registrarRequisicao("criarTransferencia", "success");
            return response;
        } catch (RestClientResponseException exception) {
            registrarErro("criarTransferencia", exception);
            throw exception;
        } catch (ResourceAccessException exception) {
            registrarErro("criarTransferencia", exception);
            throw new DownstreamUnavailableException("Monolito indisponivel para escrita de transferencia", exception);
        }
    }

    @Override
    public TransferenciaAlunoResponse buscarTransferencia(String authorization, InternalRequestContext context, UUID id) {
        try {
            TransferenciaAlunoResponse response = restClient.get()
                    .uri("/internal/transferencias/{id}", id)
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .retrieve()
                    .body(TransferenciaAlunoResponse.class);
            registrarRequisicao("buscarTransferencia", "success");
            return response;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                registrarRequisicao("buscarTransferencia", "not_found");
                throw new EnrollmentDocumentServiceResourceNotFoundException("Transferencia nao encontrada");
            }
            registrarErro("buscarTransferencia", exception);
            throw exception;
        } catch (ResourceAccessException exception) {
            registrarErro("buscarTransferencia", exception);
            throw new DownstreamUnavailableException("Monolito indisponivel para leitura de transferencia", exception);
        }
    }

    @Override
    public List<TransferenciaAlunoResponse> listarTransferenciasPorAluno(
            String authorization,
            InternalRequestContext context,
            UUID alunoId) {
        try {
            List<TransferenciaAlunoResponse> response = restClient.get()
                    .uri("/internal/transferencias/alunos/{alunoId}", alunoId)
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<TransferenciaAlunoResponse>>() {
                    });
            registrarRequisicao("listarTransferenciasPorAluno", "success");
            return response == null ? List.of() : response;
        } catch (RestClientResponseException exception) {
            registrarErro("listarTransferenciasPorAluno", exception);
            throw exception;
        } catch (ResourceAccessException exception) {
            registrarErro("listarTransferenciasPorAluno", exception);
            throw new DownstreamUnavailableException("Monolito indisponivel para leitura de transferencias do aluno", exception);
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
                "enrollment.document.shadow.monolith.requests",
                "operacao", operacao,
                "destino", "monolith",
                "resultado", resultado)
                .increment();
    }

    private void registrarErro(String operacao, Exception exception) {
        registrarRequisicao(operacao, "error");
        meterRegistry.counter(
                "enrollment.document.shadow.monolith.failures",
                "operacao", operacao,
                "causa", exception.getClass().getSimpleName())
                .increment();
    }
}
