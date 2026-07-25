package br.com.escola.bff.interfaces.rest;

import java.util.UUID;
import tools.jackson.databind.JsonNode;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.DisciplinaCreateCommand;
import br.com.escola.bff.application.dto.PeriodoLetivoCreateCommand;
import br.com.escola.bff.application.dto.SerieCreateCommand;
import br.com.escola.bff.application.dto.TurmaCreateCommand;
import br.com.escola.bff.application.dto.TurmaDisciplinaLinkCommand;
import br.com.escola.bff.application.usecase.CreateDisciplinaUseCase;
import br.com.escola.bff.application.usecase.CreatePeriodoLetivoUseCase;
import br.com.escola.bff.application.usecase.CreateSerieUseCase;
import br.com.escola.bff.application.usecase.CreateTurmaUseCase;
import br.com.escola.bff.application.usecase.LinkTurmaDisciplinaUseCase;
import br.com.escola.bff.application.usecase.CatalogoMutationWriteUseCase;
import br.com.escola.bff.application.dto.PeriodoLetivoUpdateCommand;
import br.com.escola.bff.application.dto.DisciplinaUpdateCommand;
import br.com.escola.bff.application.dto.SerieUpdateCommand;
import br.com.escola.bff.application.dto.TurmaUpdateCommand;
import br.com.escola.bff.application.dto.TurmaDisciplinaUpdateCommand;
import br.com.escola.bff.interfaces.request.DisciplinaRequest;
import br.com.escola.bff.interfaces.request.PeriodoLetivoRequest;
import br.com.escola.bff.interfaces.request.SerieRequest;
import br.com.escola.bff.interfaces.request.TurmaRequest;
import br.com.escola.bff.interfaces.request.TurmaDisciplinaRequest;
import br.com.escola.bff.interfaces.response.DisciplinaResponse;
import br.com.escola.bff.interfaces.response.PeriodoLetivoResponse;
import br.com.escola.bff.interfaces.response.SerieResponse;
import br.com.escola.bff.interfaces.response.TurmaResponse;
import br.com.escola.bff.interfaces.response.TurmaDisciplinaResponse;
import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.catalog-write-proxy-enabled", havingValue = "true")
public class CatalogoWriteController {

    private final CreatePeriodoLetivoUseCase createPeriodoLetivoUseCase;
    private final CreateDisciplinaUseCase createDisciplinaUseCase;
    private final CreateSerieUseCase createSerieUseCase;
    private final CreateTurmaUseCase createTurmaUseCase;
    private final LinkTurmaDisciplinaUseCase linkTurmaDisciplinaUseCase;
    private final CatalogoMutationWriteUseCase catalogoMutationWriteUseCase;

    public CatalogoWriteController(
            CreatePeriodoLetivoUseCase createPeriodoLetivoUseCase,
            CreateDisciplinaUseCase createDisciplinaUseCase,
            CreateSerieUseCase createSerieUseCase,
            CreateTurmaUseCase createTurmaUseCase,
            LinkTurmaDisciplinaUseCase linkTurmaDisciplinaUseCase,
            CatalogoMutationWriteUseCase catalogoMutationWriteUseCase) {
        this.createPeriodoLetivoUseCase = createPeriodoLetivoUseCase;
        this.createDisciplinaUseCase = createDisciplinaUseCase;
        this.createSerieUseCase = createSerieUseCase;
        this.createTurmaUseCase = createTurmaUseCase;
        this.linkTurmaDisciplinaUseCase = linkTurmaDisciplinaUseCase;
        this.catalogoMutationWriteUseCase = catalogoMutationWriteUseCase;
    }

    @PostMapping("/api/turnos")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<JsonNode>> criarTurno(@RequestBody java.util.Map<String, String> body, @RequestHeader(HttpHeaders.AUTHORIZATION) String a, @RequestHeader(TrustedHeaders.CORRELATION_ID) String c, @RequestHeader(name = "Idempotency-Key", required = false) String k) {
        return catalogoMutationWriteUseCase.criarTurno(query(a, c, k), body.get("codigo"), body.get("descricao")).map(x -> ResponseEntity.status(HttpStatus.CREATED).body(x));
    }

    @PutMapping("/api/turnos/{id}")
    public Mono<ResponseEntity<JsonNode>> atualizarTurno(@PathVariable UUID id, @RequestBody java.util.Map<String, String> body, @RequestHeader(HttpHeaders.AUTHORIZATION) String a, @RequestHeader(TrustedHeaders.CORRELATION_ID) String c, @RequestHeader(name = "Idempotency-Key", required = false) String k) {
        return catalogoMutationWriteUseCase.atualizarTurno(id, query(a, c, k), body.get("codigo"), body.get("descricao")).map(ResponseEntity::ok);
    }

    @DeleteMapping("/api/periodos-letivos/{id}")
    public Mono<ResponseEntity<Void>> excluirPeriodoLetivo(@PathVariable UUID id, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId, @RequestHeader(name = "Idempotency-Key", required = false) String key) {
        return catalogoMutationWriteUseCase.excluirPeriodo(id, query(authorization, correlationId, key)).thenReturn(ResponseEntity.noContent().build());
    }

    @DeleteMapping("/api/disciplinas/{id}")
    public Mono<ResponseEntity<Void>> excluirDisciplina(@PathVariable UUID id, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId, @RequestHeader(name = "Idempotency-Key", required = false) String key) {
        return catalogoMutationWriteUseCase.excluirDisciplina(id, query(authorization, correlationId, key)).thenReturn(ResponseEntity.noContent().build());
    }

    @DeleteMapping("/api/series/{id}")
    public Mono<ResponseEntity<Void>> excluirSerie(@PathVariable UUID id, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId, @RequestHeader(name = "Idempotency-Key", required = false) String key) {
        return catalogoMutationWriteUseCase.excluirSerie(id, query(authorization, correlationId, key)).thenReturn(ResponseEntity.noContent().build());
    }

    @DeleteMapping("/api/turmas/{id}")
    public Mono<ResponseEntity<Void>> excluirTurma(@PathVariable UUID id, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId, @RequestHeader(name = "Idempotency-Key", required = false) String key) {
        return catalogoMutationWriteUseCase.excluirTurma(id, query(authorization, correlationId, key)).thenReturn(ResponseEntity.noContent().build());
    }

    @PutMapping("/api/periodos-letivos/{id}")
    public Mono<ResponseEntity<PeriodoLetivoResponse>> atualizarPeriodoLetivo(@PathVariable UUID id, @Valid @RequestBody PeriodoLetivoRequest r, @RequestHeader(HttpHeaders.AUTHORIZATION) String a, @RequestHeader(TrustedHeaders.CORRELATION_ID) String c, @RequestHeader(name = "Idempotency-Key", required = false) String k) {
        return catalogoMutationWriteUseCase.atualizarPeriodo(id, query(a,c,k), new PeriodoLetivoUpdateCommand(r.nome(),r.ano(),r.dataInicio(),r.dataFim(),true))
                .map(x -> ResponseEntity.ok(new PeriodoLetivoResponse(x.id(),x.nome(),x.ano(),x.dataInicio(),x.dataFim(),x.ativo(),x.escolaId(),x.escolaNome(),x.createdAt())));
    }

    @PutMapping("/api/disciplinas/{id}")
    public Mono<ResponseEntity<DisciplinaResponse>> atualizarDisciplina(@PathVariable UUID id, @Valid @RequestBody DisciplinaRequest r, @RequestHeader(HttpHeaders.AUTHORIZATION) String a, @RequestHeader(TrustedHeaders.CORRELATION_ID) String c, @RequestHeader(name = "Idempotency-Key", required = false) String k) {
        return catalogoMutationWriteUseCase.atualizarDisciplina(id, query(a,c,k), new DisciplinaUpdateCommand(r.nome(),r.cargaHoraria(),r.status()))
                .map(x -> ResponseEntity.ok(new DisciplinaResponse(x.id(),x.nome(),x.cargaHoraria(),x.status(),x.escolaId(),x.escolaNome(),x.createdAt())));
    }

    @PutMapping("/api/series/{id}")
    public Mono<ResponseEntity<SerieResponse>> atualizarSerie(@PathVariable UUID id, @Valid @RequestBody SerieRequest r, @RequestHeader(HttpHeaders.AUTHORIZATION) String a, @RequestHeader(TrustedHeaders.CORRELATION_ID) String c, @RequestHeader(name = "Idempotency-Key", required = false) String k) {
        return catalogoMutationWriteUseCase.atualizarSerie(id, query(a,c,k), new SerieUpdateCommand(r.nome(),r.ordem(),r.nivelEnsino()))
                .map(x -> ResponseEntity.ok(new SerieResponse(x.id(),x.nome(),x.ordem(),x.nivelEnsino(),x.escolaId(),x.escolaNome(),x.createdAt())));
    }

    @PutMapping("/api/turmas/{id}")
    public Mono<ResponseEntity<TurmaResponse>> atualizarTurma(@PathVariable UUID id, @Valid @RequestBody TurmaRequest r, @RequestHeader(HttpHeaders.AUTHORIZATION) String a, @RequestHeader(TrustedHeaders.CORRELATION_ID) String c, @RequestHeader(name = "Idempotency-Key", required = false) String k) {
        return catalogoMutationWriteUseCase.atualizarTurma(id, query(a,c,k), new TurmaUpdateCommand(r.codigo(),r.nome(),r.capacidade(),r.periodoLetivoId(),r.serieId(),r.turno(),r.status()))
                .map(x -> ResponseEntity.ok(new TurmaResponse(x.id(),x.codigo(),x.nome(),x.capacidade(),x.periodoLetivoId(),x.serieId(),x.serieNome(),x.turno(),x.status(),x.escolaId(),x.escolaNome(),x.createdAt())));
    }

    @PutMapping("/api/turmas/{turmaId}/disciplinas/{vinculoId}")
    public Mono<ResponseEntity<TurmaDisciplinaResponse>> atualizarVinculoDisciplina(@PathVariable UUID turmaId, @PathVariable UUID vinculoId, @Valid @RequestBody TurmaDisciplinaRequest r, @RequestHeader(HttpHeaders.AUTHORIZATION) String a, @RequestHeader(TrustedHeaders.CORRELATION_ID) String c, @RequestHeader(name = "Idempotency-Key", required = false) String k) {
        return catalogoMutationWriteUseCase.atualizarVinculo(turmaId, vinculoId, query(a,c,k), new TurmaDisciplinaUpdateCommand(r.cargaHoraria()))
                .map(x -> ResponseEntity.ok(new TurmaDisciplinaResponse(x.id(),x.turmaId(),x.disciplinaId(),x.disciplinaNome(),x.cargaHoraria(),x.createdAt())));
    }

    @DeleteMapping("/api/turmas/{turmaId}/disciplinas/{vinculoId}")
    public Mono<ResponseEntity<Void>> desvincularDisciplina(@PathVariable UUID turmaId, @PathVariable UUID vinculoId, @RequestHeader(HttpHeaders.AUTHORIZATION) String a, @RequestHeader(TrustedHeaders.CORRELATION_ID) String c, @RequestHeader(name = "Idempotency-Key", required = false) String k) {
        return catalogoMutationWriteUseCase.desvincular(turmaId, vinculoId, query(a,c,k)).thenReturn(ResponseEntity.noContent().build());
    }

    @PostMapping("/api/periodos-letivos")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<PeriodoLetivoResponse>> criarPeriodoLetivo(
            @Valid @RequestBody PeriodoLetivoRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId,
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey) {
        String resolvedKey = idempotencyKey != null && !idempotencyKey.isBlank()
                ? idempotencyKey
                : UUID.randomUUID().toString();
        return createPeriodoLetivoUseCase.executar(
                        new CatalogWriteQuery(authorization, correlationId, resolvedKey),
                        new PeriodoLetivoCreateCommand(
                                request.nome(),
                                request.ano(),
                                request.dataInicio(),
                                request.dataFim(),
                                request.escolaId()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(new PeriodoLetivoResponse(
                        body.id(),
                        body.nome(),
                        body.ano(),
                        body.dataInicio(),
                        body.dataFim(),
                        body.ativo(),
                        body.escolaId(),
                        body.escolaNome(),
                        body.createdAt())));
    }

    @PostMapping("/api/disciplinas")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<DisciplinaResponse>> criarDisciplina(
            @Valid @RequestBody DisciplinaRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId,
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey) {
        String resolvedKey = idempotencyKey != null && !idempotencyKey.isBlank()
                ? idempotencyKey
                : UUID.randomUUID().toString();
        return createDisciplinaUseCase.executar(
                        new CatalogWriteQuery(authorization, correlationId, resolvedKey),
                        new DisciplinaCreateCommand(
                                request.nome(),
                                request.cargaHoraria(),
                                request.status(),
                                request.escolaId()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(new DisciplinaResponse(
                        body.id(),
                        body.nome(),
                        body.cargaHoraria(),
                        body.status(),
                        body.escolaId(),
                        body.escolaNome(),
                        body.createdAt())));
    }

    @PostMapping("/api/series")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<SerieResponse>> criarSerie(
            @Valid @RequestBody SerieRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId,
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey) {
        String resolvedKey = idempotencyKey != null && !idempotencyKey.isBlank()
                ? idempotencyKey
                : UUID.randomUUID().toString();
        return createSerieUseCase.executar(
                        new CatalogWriteQuery(authorization, correlationId, resolvedKey),
                        new SerieCreateCommand(
                                request.nome(),
                                request.ordem(),
                                request.nivelEnsino(),
                                request.escolaId()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(new SerieResponse(
                        body.id(),
                        body.nome(),
                        body.ordem(),
                        body.nivelEnsino(),
                        body.escolaId(),
                        body.escolaNome(),
                        body.createdAt())));
    }

    @PostMapping("/api/turmas")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<TurmaResponse>> criarTurma(
            @Valid @RequestBody TurmaRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId,
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey) {
        String resolvedKey = idempotencyKey != null && !idempotencyKey.isBlank()
                ? idempotencyKey
                : UUID.randomUUID().toString();
        return createTurmaUseCase.executar(
                        new CatalogWriteQuery(authorization, correlationId, resolvedKey),
                        new TurmaCreateCommand(
                                request.codigo(),
                                request.nome(),
                                request.capacidade(),
                                request.periodoLetivoId(),
                                request.serieId(),
                                request.turno(),
                                request.status(),
                                request.escolaId()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(new TurmaResponse(
                        body.id(),
                        body.codigo(),
                        body.nome(),
                        body.capacidade(),
                        body.periodoLetivoId(),
                        body.serieId(),
                        body.serieNome(),
                        body.turno(),
                        body.status(),
                        body.escolaId(),
                        body.escolaNome(),
                        body.createdAt())));
    }

    @PostMapping("/api/turmas/{turmaId}/disciplinas")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<TurmaDisciplinaResponse>> vincularDisciplinaNaTurma(
            @org.springframework.web.bind.annotation.PathVariable UUID turmaId,
            @Valid @RequestBody TurmaDisciplinaRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId,
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey) {
        String resolvedKey = idempotencyKey != null && !idempotencyKey.isBlank()
                ? idempotencyKey
                : UUID.randomUUID().toString();
        return linkTurmaDisciplinaUseCase.executar(
                        new CatalogWriteQuery(authorization, correlationId, resolvedKey),
                        new TurmaDisciplinaLinkCommand(
                                turmaId,
                                request.disciplinaId(),
                                request.cargaHoraria()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(new TurmaDisciplinaResponse(
                        body.id(),
                        body.turmaId(),
                        body.disciplinaId(),
                        body.disciplinaNome(),
                        body.cargaHoraria(),
                        body.createdAt())));
    }

    private CatalogWriteQuery query(String authorization, String correlationId, String idempotencyKey) {
        return new CatalogWriteQuery(authorization, correlationId,
                idempotencyKey != null && !idempotencyKey.isBlank() ? idempotencyKey : UUID.randomUUID().toString());
    }
}

