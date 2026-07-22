package br.com.escola.bff.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.DisciplinaCreatedResult;
import br.com.escola.bff.application.dto.DisciplinaUpdateCommand;
import br.com.escola.bff.application.dto.PeriodoLetivoCreatedResult;
import br.com.escola.bff.application.dto.PeriodoLetivoUpdateCommand;
import br.com.escola.bff.application.dto.SerieCreatedResult;
import br.com.escola.bff.application.dto.SerieUpdateCommand;
import br.com.escola.bff.application.dto.TurmaCreatedResult;
import br.com.escola.bff.application.dto.TurmaDisciplinaLinkedResult;
import br.com.escola.bff.application.dto.TurmaDisciplinaUpdateCommand;
import br.com.escola.bff.application.dto.TurmaUpdateCommand;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.CatalogoMutationWritePort;
import br.com.escola.bff.application.port.out.CatalogoNivelEnsinoResolverPort;
import br.com.escola.bff.application.port.out.CatalogoTurnoResolverPort;
import br.com.escola.bff.application.port.out.CatalogWriteObservabilityPort;
import br.com.escola.bff.application.usecase.CatalogoMutationWriteUseCase;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;

public class CatalogoMutationWriteRoutingService implements CatalogoMutationWriteUseCase {
    private final CatalogoMutationWritePort port;
    private final AuthContextPort auth;
    private final CatalogoNivelEnsinoResolverPort niveis;
    private final CatalogoTurnoResolverPort turnos;
    private final CatalogWriteObservabilityPort observability;

    public CatalogoMutationWriteRoutingService(CatalogoMutationWritePort port, AuthContextPort auth,
            CatalogoNivelEnsinoResolverPort niveis, CatalogoTurnoResolverPort turnos,
            CatalogWriteObservabilityPort observability) {
        this.port = port; this.auth = auth; this.niveis = niveis; this.turnos = turnos;
        this.observability = observability;
    }

    @Override public Mono<PeriodoLetivoCreatedResult> atualizarPeriodo(UUID id, CatalogWriteQuery q, PeriodoLetivoUpdateCommand c) {
        return observed(CatalogWriteRoute.PERIODOS_LETIVOS, context(q).flatMap(x -> port.executar(HttpMethod.PUT, "/internal/v1/periodos-letivos/" + id,
                map("nome", c.nome(), "ano", c.ano() == null ? c.dataInicio().getYear() : c.ano(), "dataInicio", c.dataInicio(), "dataFim", c.dataFim(), "ativo", c.ativo()), q, x)
                .map(r -> periodo(r.body(), x))));
    }
    @Override public Mono<Void> excluirPeriodo(UUID id, CatalogWriteQuery q) { return delete(CatalogWriteRoute.PERIODOS_LETIVOS, "/internal/v1/periodos-letivos/" + id, q); }
    @Override public Mono<DisciplinaCreatedResult> atualizarDisciplina(UUID id, CatalogWriteQuery q, DisciplinaUpdateCommand c) {
        return observed(CatalogWriteRoute.DISCIPLINAS, context(q).flatMap(x -> port.executar(HttpMethod.PUT, "/internal/v1/disciplinas/" + id,
                map("nome", c.nome(), "cargaHoraria", c.cargaHoraria(), "ativo", !"INATIVA".equalsIgnoreCase(c.status())), q, x)
                .map(r -> disciplina(r.body(), x))));
    }
    @Override public Mono<Void> excluirDisciplina(UUID id, CatalogWriteQuery q) { return delete(CatalogWriteRoute.DISCIPLINAS, "/internal/v1/disciplinas/" + id, q); }
    @Override public Mono<SerieCreatedResult> atualizarSerie(UUID id, CatalogWriteQuery q, SerieUpdateCommand c) {
        return observed(CatalogWriteRoute.SERIES,
                context(q).flatMap(x -> niveis.resolve(q, x, c.nivelEnsino())
                        .flatMap(n -> port.executar(HttpMethod.PUT, "/internal/v1/series/" + id,
                                map("nome", c.nome(), "ordem", c.ordem(), "nivelEnsinoId", n.id()), q, x)
                                .map(r -> serie(r.body(), x)))));
    }
    @Override public Mono<Void> excluirSerie(UUID id, CatalogWriteQuery q) { return delete(CatalogWriteRoute.SERIES, "/internal/v1/series/" + id, q); }
    @Override public Mono<TurmaCreatedResult> atualizarTurma(UUID id, CatalogWriteQuery q, TurmaUpdateCommand c) {
        return observed(CatalogWriteRoute.TURMAS,
                context(q).flatMap(x -> turnos.resolve(q, x, c.turno())
                        .flatMap(t -> port.executar(HttpMethod.PUT, "/internal/v1/turmas/" + id,
                                map("codigo", c.codigo(), "nome", c.nome(), "capacidade", c.capacidade(), "periodoLetivoId", c.periodoLetivoId(), "serieId", c.serieId(), "turnoId", t.id(), "ativo", !"INATIVA".equalsIgnoreCase(c.status())), q, x)
                                .map(r -> turma(r.body(), x)))));
    }
    @Override public Mono<Void> excluirTurma(UUID id, CatalogWriteQuery q) { return delete(CatalogWriteRoute.TURMAS, "/internal/v1/turmas/" + id, q); }
    @Override public Mono<TurmaDisciplinaLinkedResult> atualizarVinculo(UUID turmaId, UUID vinculoId, CatalogWriteQuery q, TurmaDisciplinaUpdateCommand c) {
        return observed(CatalogWriteRoute.TURMA_DISCIPLINAS,
                context(q).flatMap(x -> port.executar(HttpMethod.PUT, "/internal/v1/turmas/" + turmaId + "/disciplinas/" + vinculoId,
                        map("cargaHoraria", c.cargaHoraria()), q, x).map(r -> vinculo(r.body()))));
    }
    @Override public Mono<Void> desvincular(UUID turmaId, UUID vinculoId, CatalogWriteQuery q) { return delete(CatalogWriteRoute.TURMA_DISCIPLINAS, "/internal/v1/turmas/" + turmaId + "/disciplinas/" + vinculoId, q); }

    private Mono<Void> delete(CatalogWriteRoute route, String path, CatalogWriteQuery q) { return observed(route, context(q).flatMap(x -> port.executar(HttpMethod.DELETE, path, null, q, x)).then()); }
    private <T> Mono<T> observed(CatalogWriteRoute route, Mono<T> operation) {
        CatalogWriteCutoverDecision decision = new CatalogWriteCutoverDecision(route, true, "catalog_official");
        return operation.doOnSuccess(ignored -> observability.recordCatalogSuccess(decision))
                .doOnError(error -> observability.recordCatalogFailure(decision, error));
    }
    private Mono<AuthSessionContext> context(CatalogWriteQuery q) { return auth.resolve(new CatalogReadQuery(q.authorization(), q.correlationId())); }
    private Map<String,Object> map(Object... values) { Map<String,Object> map = new HashMap<>(); for (int i=0;i<values.length;i+=2) map.put((String)values[i], values[i+1]); return map; }
    private PeriodoLetivoCreatedResult periodo(JsonNode n, AuthSessionContext x) { return new PeriodoLetivoCreatedResult(UUID.fromString(n.get("id").asText()), n.get("nome").asText(), n.get("ano").asInt(), LocalDate.parse(n.get("dataInicio").asText()), LocalDate.parse(n.get("dataFim").asText()), n.get("ativo").asBoolean(), UUID.fromString(n.get("escolaId").asText()), x.escolaNome(), LocalDateTime.parse(n.get("createdAt").asText())); }
    private DisciplinaCreatedResult disciplina(JsonNode n, AuthSessionContext x) { return new DisciplinaCreatedResult(UUID.fromString(n.get("id").asText()), n.get("nome").asText(), n.path("cargaHoraria").isMissingNode() || n.path("cargaHoraria").isNull()?null:n.get("cargaHoraria").asInt(), n.get("ativo").asBoolean()?"ATIVA":"INATIVA", UUID.fromString(n.get("escolaId").asText()), x.escolaNome(), LocalDateTime.parse(n.get("createdAt").asText())); }
    private SerieCreatedResult serie(JsonNode n, AuthSessionContext x) { return new SerieCreatedResult(UUID.fromString(n.get("id").asText()), n.get("nome").asText(), n.get("ordem").asInt(), n.get("nivelEnsinoCodigo").asText(), UUID.fromString(n.get("escolaId").asText()), x.escolaNome(), LocalDateTime.parse(n.get("createdAt").asText())); }
    private TurmaCreatedResult turma(JsonNode n, AuthSessionContext x) { return new TurmaCreatedResult(UUID.fromString(n.get("id").asText()), n.get("codigo").asText(), n.get("nome").asText(), n.get("capacidade").asInt(), UUID.fromString(n.get("periodoLetivoId").asText()), UUID.fromString(n.get("serieId").asText()), n.get("serieNome").asText(), n.get("turnoCodigo").asText(), n.get("ativo").asBoolean()?"ATIVA":"INATIVA", UUID.fromString(n.get("escolaId").asText()), x.escolaNome(), LocalDateTime.parse(n.get("createdAt").asText())); }
    private TurmaDisciplinaLinkedResult vinculo(JsonNode n) { return new TurmaDisciplinaLinkedResult(UUID.fromString(n.get("id").asText()), UUID.fromString(n.get("turmaId").asText()), UUID.fromString(n.get("disciplinaId").asText()), n.get("disciplinaNome").asText(), n.path("cargaHoraria").isMissingNode() || n.path("cargaHoraria").isNull()?null:n.get("cargaHoraria").asInt(), LocalDateTime.parse(n.get("createdAt").asText())); }
}
