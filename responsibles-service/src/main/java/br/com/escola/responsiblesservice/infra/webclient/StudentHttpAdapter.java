package br.com.escola.responsiblesservice.infra.webclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import br.com.escola.responsiblesservice.application.context.InternalHeaders;
import br.com.escola.responsiblesservice.application.context.InternalRequestContext;
import br.com.escola.responsiblesservice.application.exception.AlunoDependenciaIndisponivelException;
import br.com.escola.responsiblesservice.application.port.out.AlunoConsultaPort;
import br.com.escola.responsiblesservice.infra.config.StudentClientProperties;

@Component
public class StudentHttpAdapter implements AlunoConsultaPort {

    private final RestClient restClient;
    private final StudentClientProperties properties;

    public StudentHttpAdapter(
            @Qualifier("peopleStudentRestClient") RestClient restClient,
            StudentClientProperties properties) {
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
            throw new AlunoDependenciaIndisponivelException(
                    "people-service rejeitou a validacao interna do aluno", exception);
        } catch (ResourceAccessException exception) {
            throw new AlunoDependenciaIndisponivelException(
                    "people-service indisponivel para validacao interna do aluno", exception);
        }
    }

    private void addHeaders(HttpHeaders headers, InternalRequestContext context) {
        headers.set(InternalHeaders.INTERNAL_TOKEN, properties.internalToken());
        headers.set(InternalHeaders.CORRELATION_ID, context.correlationId());
        headers.set(InternalHeaders.USUARIO_ID, context.usuarioId().toString());
        headers.set(InternalHeaders.ESCOLA_ID, context.escolaId().toString());
    }
}
