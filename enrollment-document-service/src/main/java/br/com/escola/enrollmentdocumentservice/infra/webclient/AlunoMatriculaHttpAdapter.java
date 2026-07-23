package br.com.escola.enrollmentdocumentservice.infra.webclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import br.com.escola.enrollmentdocumentservice.application.context.InternalHeaders;
import br.com.escola.enrollmentdocumentservice.application.context.InternalRequestContext;
import br.com.escola.enrollmentdocumentservice.application.exception.MatriculaDependenciaIndisponivelException;
import br.com.escola.enrollmentdocumentservice.application.port.out.AlunoMatriculaPort;
import br.com.escola.enrollmentdocumentservice.infra.config.AlunoMatriculaClientProperties;

@Component
public class AlunoMatriculaHttpAdapter implements AlunoMatriculaPort {

    private final RestClient restClient;
    private final AlunoMatriculaClientProperties properties;

    public AlunoMatriculaHttpAdapter(
            @Qualifier("matriculaAlunoRestClient") RestClient restClient,
            AlunoMatriculaClientProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @Override
    public boolean existeAtivoNaEscola(UUID alunoId, InternalRequestContext context) {
        try {
            restClient.get()
                    .uri("/internal/v1/alunos/{alunoId}", alunoId)
                    .headers(headers -> addHeaders(headers, context))
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                return false;
            }
            throw unavailable("people-service rejeitou a validacao interna do aluno", exception);
        } catch (ResourceAccessException exception) {
            throw unavailable("people-service indisponivel para validacao interna do aluno", exception);
        }
    }

    private MatriculaDependenciaIndisponivelException unavailable(String message, RuntimeException cause) {
        return new MatriculaDependenciaIndisponivelException(message, cause);
    }

    private void addHeaders(HttpHeaders headers, InternalRequestContext context) {
        headers.set(InternalHeaders.INTERNAL_TOKEN, properties.internalToken());
        headers.set(InternalHeaders.CORRELATION_ID, context.correlationId());
        headers.set(InternalHeaders.USUARIO_ID, context.usuarioId().toString());
        headers.set(InternalHeaders.ESCOLA_ID, context.escolaId().toString());
    }
}
