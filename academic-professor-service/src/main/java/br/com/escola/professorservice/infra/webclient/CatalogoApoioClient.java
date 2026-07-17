package br.com.escola.professorservice.infra.webclient;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import br.com.escola.professorservice.application.context.InternalHeaders;
import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.exception.DownstreamUnavailableException;
import br.com.escola.professorservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.professorservice.application.port.out.CatalogoApoioPort;
import br.com.escola.professorservice.infra.config.CatalogoApoioClientProperties;

@Component
public class CatalogoApoioClient implements CatalogoApoioPort {

    private final RestClient restClient;
    private final CatalogoApoioClientProperties properties;

    public CatalogoApoioClient(
            @Qualifier("catalogoApoioRestClient") RestClient catalogoApoioRestClient,
            CatalogoApoioClientProperties properties) {
        this.restClient = catalogoApoioRestClient;
        this.properties = properties;
    }

    @Override
    public TurmaDisciplinaResumo buscarTurmaDisciplina(
            String authorization,
            InternalRequestContext context,
            UUID turmaDisciplinaId) {
        try {
            TurmaDisciplinaResponse response = restClient.get()
                    .uri("/internal/v1/turmas-disciplinas/{id}", turmaDisciplinaId)
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .retrieve()
                    .body(TurmaDisciplinaResponse.class);
            if (response == null) {
                throw new DownstreamUnavailableException("Academic catalog service retornou resposta vazia para turma-disciplina");
            }
            return response.toPort();
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new RecursoNaoEncontradoException("Turma disciplina não encontrada");
            }
            throw new DownstreamUnavailableException("Academic catalog service rejeitou consulta de turma-disciplina", exception);
        } catch (ResourceAccessException exception) {
            throw new DownstreamUnavailableException("Academic catalog service indisponivel para consulta de turma-disciplina", exception);
        }
    }

    @Override
    public TurmaResumo buscarTurma(String authorization, InternalRequestContext context, UUID turmaId) {
        try {
            TurmaResponse response = restClient.get()
                    .uri("/internal/v1/turmas/{id}", turmaId)
                    .headers(headers -> enrichHeaders(headers, authorization, context))
                    .retrieve()
                    .body(TurmaResponse.class);
            if (response == null) {
                throw new DownstreamUnavailableException("Academic catalog service retornou resposta vazia para turma");
            }
            return response.toPort();
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new RecursoNaoEncontradoException("Turma não encontrada");
            }
            throw new DownstreamUnavailableException("Academic catalog service rejeitou consulta de turma", exception);
        } catch (ResourceAccessException exception) {
            throw new DownstreamUnavailableException("Academic catalog service indisponivel para consulta de turma", exception);
        }
    }

    private void enrichHeaders(HttpHeaders headers, String authorization, InternalRequestContext context) {
        headers.set(HttpHeaders.AUTHORIZATION, authorization);
        headers.set(InternalHeaders.INTERNAL_TOKEN, properties.internalToken());
        headers.set(InternalHeaders.CORRELATION_ID, context.correlationId());
        headers.set(InternalHeaders.USUARIO_ID, context.usuarioId().toString());
        headers.set(InternalHeaders.ESCOLA_ID, context.escolaId().toString());
    }

    private record TurmaDisciplinaResponse(
            UUID id,
            UUID turmaId,
            UUID disciplinaId,
            String disciplinaNome,
            Integer cargaHoraria,
            UUID escolaId) {
        TurmaDisciplinaResumo toPort() {
            return new TurmaDisciplinaResumo(id, turmaId, disciplinaId, disciplinaNome, cargaHoraria, escolaId);
        }
    }

    private record TurmaResponse(
            UUID id,
            String codigo,
            String nome,
            Integer capacidade,
            UUID periodoLetivoId,
            UUID serieId,
            String serieNome,
            UUID turnoId,
            String turnoCodigo,
            Boolean ativo,
            UUID escolaId) {
        TurmaResumo toPort() {
            return new TurmaResumo(id, codigo, nome, escolaId);
        }
    }
}
