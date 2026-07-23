package br.com.escola.bff.application.service;

import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.PlanejamentoBimestralReadPort;
import br.com.escola.bff.application.port.out.ProfessorReadPort;
import br.com.escola.bff.application.usecase.ConsultarPlanejamentoBimestralUseCase;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

public class PlanejamentoBimestralReadProxyService implements ConsultarPlanejamentoBimestralUseCase {

    private final AuthContextPort authContextPort;
    private final PlanejamentoBimestralReadPort planejamentoBimestralReadPort;
    private final ProfessorReadPort professorReadPort;
    private final ObjectMapper objectMapper;

    public PlanejamentoBimestralReadProxyService(
            AuthContextPort authContextPort,
            PlanejamentoBimestralReadPort planejamentoBimestralReadPort,
            ProfessorReadPort professorReadPort,
            ObjectMapper objectMapper) {
        this.authContextPort = authContextPort;
        this.planejamentoBimestralReadPort = planejamentoBimestralReadPort;
        this.professorReadPort = professorReadPort;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<ResponseEntity<String>> listar(
            String authorization,
            String correlationId,
            UUID professorTurmaDisciplinaId,
            UUID periodoAvaliativoId,
            UUID professorId,
            UUID turmaId,
            UUID disciplinaId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> professorTurmaDisciplinaId != null || (professorId == null && turmaId == null && disciplinaId == null)
                        ? planejamentoBimestralReadPort.listar(professorTurmaDisciplinaId, periodoAvaliativoId, query, context)
                        : listarComFiltrosLegados(professorId, turmaId, disciplinaId, periodoAvaliativoId, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> buscarPorId(String authorization, String correlationId, UUID planejamentoId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> planejamentoBimestralReadPort.buscarPorId(planejamentoId, query, context));
    }

    private Mono<ResponseEntity<String>> listarComFiltrosLegados(
            UUID professorId, UUID turmaId, UUID disciplinaId, UUID periodoAvaliativoId,
            CatalogReadQuery query, br.com.escola.bff.application.dto.AuthSessionContext context) {
        return resolverAlocacoes(professorId, turmaId, query, context).flatMap(response -> {
            if (!response.getStatusCode().is2xxSuccessful()) {
                return Mono.just(ResponseEntity.status(response.getStatusCode()).body(response.getBody()));
            }
            List<UUID> vinculos = vinculosCompativeis(response.getBody(), professorId, turmaId, disciplinaId);
            return Flux.fromIterable(vinculos)
                    .concatMap(vinculoId -> planejamentoBimestralReadPort.listar(vinculoId, periodoAvaliativoId, query, context))
                    .collectList()
                    .flatMap(this::unificarRespostas);
        });
    }

    private Mono<ResponseEntity<String>> resolverAlocacoes(
            UUID professorId, UUID turmaId, CatalogReadQuery query,
            br.com.escola.bff.application.dto.AuthSessionContext context) {
        if (professorId != null) {
            return professorReadPort.listarAlocacoesPorProfessor(professorId, query, context);
        }
        if (turmaId != null) {
            return professorReadPort.listarProfessoresPorTurma(turmaId, query, context);
        }
        return professorReadPort.listarProfessores(query, context).flatMap(professores -> {
            if (!professores.getStatusCode().is2xxSuccessful()) {
                return Mono.just(professores);
            }
            try {
                List<UUID> ids = new ArrayList<>();
                for (JsonNode professor : objectMapper.readTree(professores.getBody())) {
                    ids.add(UUID.fromString(professor.path("id").asText()));
                }
                return Flux.fromIterable(ids)
                        .concatMap(id -> professorReadPort.listarAlocacoesPorProfessor(id, query, context))
                        .collectList()
                        .flatMap(this::unificarAlocacoes);
            } catch (Exception exception) {
                return Mono.error(new IllegalStateException("Resposta de professores invalida", exception));
            }
        });
    }

    private Mono<ResponseEntity<String>> unificarAlocacoes(List<ResponseEntity<String>> respostas) {
        ResponseEntity<String> erro = respostas.stream().filter(response -> !response.getStatusCode().is2xxSuccessful())
                .findFirst().orElse(null);
        if (erro != null) {
            return Mono.just(ResponseEntity.status(erro.getStatusCode()).body(erro.getBody()));
        }
        try {
            List<JsonNode> alocacoes = new ArrayList<>();
            for (ResponseEntity<String> resposta : respostas) {
                objectMapper.readTree(resposta.getBody()).forEach(alocacoes::add);
            }
            return Mono.just(ResponseEntity.ok(objectMapper.writeValueAsString(alocacoes)));
        } catch (Exception exception) {
            return Mono.error(new IllegalStateException("Resposta de alocacoes academicas invalida", exception));
        }
    }

    private List<UUID> vinculosCompativeis(String body, UUID professorId, UUID turmaId, UUID disciplinaId) {
        try {
            List<UUID> ids = new ArrayList<>();
            for (JsonNode alocacao : objectMapper.readTree(body)) {
                if ((professorId == null || professorId.toString().equals(alocacao.path("professorId").asText()))
                        && (turmaId == null || turmaId.toString().equals(alocacao.path("turmaId").asText()))
                        && (disciplinaId == null || disciplinaId.toString().equals(alocacao.path("disciplinaId").asText()))) {
                    ids.add(UUID.fromString(alocacao.path("turmaDisciplinaId").asText()));
                }
            }
            return ids;
        } catch (Exception exception) {
            throw new IllegalStateException("Resposta de alocacoes academicas invalida", exception);
        }
    }

    private Mono<ResponseEntity<String>> unificarRespostas(List<ResponseEntity<String>> respostas) {
        ResponseEntity<String> erro = respostas.stream().filter(response -> !response.getStatusCode().is2xxSuccessful())
                .findFirst().orElse(null);
        if (erro != null) {
            return Mono.just(ResponseEntity.status(erro.getStatusCode()).body(erro.getBody()));
        }
        try {
            List<JsonNode> planejamentos = new ArrayList<>();
            for (ResponseEntity<String> resposta : respostas) {
                objectMapper.readTree(resposta.getBody()).forEach(planejamentos::add);
            }
            return Mono.just(ResponseEntity.ok(objectMapper.writeValueAsString(planejamentos)));
        } catch (Exception exception) {
            return Mono.error(new IllegalStateException("Resposta de planejamentos invalida", exception));
        }
    }
}
