package br.com.escola.enrollmentdocumentservice.infra.webclient;

import java.util.UUID;
import java.util.Optional;

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
import br.com.escola.enrollmentdocumentservice.application.port.out.CatalogoAcademicoMatriculaPort;
import br.com.escola.enrollmentdocumentservice.application.dto.TurmaMatriculaResumo;
import br.com.escola.enrollmentdocumentservice.infra.config.CatalogoAcademicoMatriculaClientProperties;

@Component
public class CatalogoAcademicoMatriculaHttpAdapter implements CatalogoAcademicoMatriculaPort {

    private final RestClient restClient;
    private final CatalogoAcademicoMatriculaClientProperties properties;

    public CatalogoAcademicoMatriculaHttpAdapter(
            @Qualifier("matriculaCatalogoAcademicoRestClient") RestClient restClient,
            CatalogoAcademicoMatriculaClientProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @Override
    public Optional<TurmaMatriculaResumo> buscarTurmaNaEscola(UUID turmaId, InternalRequestContext context) {
        try {
            TurmaMatriculaResponse response = restClient.get()
                    .uri("/internal/v1/turmas/{id}", turmaId)
                    .headers(headers -> addHeaders(headers, context))
                    .retrieve()
                    .body(TurmaMatriculaResponse.class);
            return response == null
                    ? Optional.empty()
                    : Optional.of(new TurmaMatriculaResumo(response.serieId(), response.periodoLetivoId()));
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            }
            throw new MatriculaDependenciaIndisponivelException(
                    "academic-catalog-service rejeitou a validacao interna da matricula", exception);
        } catch (ResourceAccessException exception) {
            throw new MatriculaDependenciaIndisponivelException(
                    "academic-catalog-service indisponivel para validacao interna da matricula", exception);
        }
    }

    @Override
    public boolean existeSerieNaEscola(UUID serieId, InternalRequestContext context) {
        return existe("/internal/v1/series/{id}", serieId, context);
    }

    @Override
    public boolean existePeriodoLetivoAtivoNaEscola(UUID periodoLetivoId, InternalRequestContext context) {
        try {
            PeriodoLetivoMatriculaResponse response = restClient.get()
                    .uri("/internal/v1/periodos-letivos/{id}", periodoLetivoId)
                    .headers(headers -> addHeaders(headers, context))
                    .retrieve()
                    .body(PeriodoLetivoMatriculaResponse.class);
            return response != null && response.ativo();
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                return false;
            }
            throw new MatriculaDependenciaIndisponivelException(
                    "academic-catalog-service rejeitou a validacao interna da matricula", exception);
        } catch (ResourceAccessException exception) {
            throw new MatriculaDependenciaIndisponivelException(
                    "academic-catalog-service indisponivel para validacao interna da matricula", exception);
        }
    }

    private boolean existe(String uri, UUID id, InternalRequestContext context) {
        try {
            restClient.get()
                    .uri(uri, id)
                    .headers(headers -> addHeaders(headers, context))
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                return false;
            }
            throw new MatriculaDependenciaIndisponivelException(
                    "academic-catalog-service rejeitou a validacao interna da matricula", exception);
        } catch (ResourceAccessException exception) {
            throw new MatriculaDependenciaIndisponivelException(
                    "academic-catalog-service indisponivel para validacao interna da matricula", exception);
        }
    }

    private void addHeaders(HttpHeaders headers, InternalRequestContext context) {
        headers.set(InternalHeaders.INTERNAL_TOKEN, properties.internalToken());
        headers.set(InternalHeaders.CORRELATION_ID, context.correlationId());
        headers.set(InternalHeaders.USUARIO_ID, context.usuarioId().toString());
        headers.set(InternalHeaders.ESCOLA_ID, context.escolaId().toString());
    }

    private record PeriodoLetivoMatriculaResponse(boolean ativo) {
    }

    private record TurmaMatriculaResponse(UUID serieId, UUID periodoLetivoId) {
    }
}
