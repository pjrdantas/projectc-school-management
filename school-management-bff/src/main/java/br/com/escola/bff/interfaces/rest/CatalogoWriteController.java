package br.com.escola.bff.interfaces.rest;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
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

    public CatalogoWriteController(
            CreatePeriodoLetivoUseCase createPeriodoLetivoUseCase,
            CreateDisciplinaUseCase createDisciplinaUseCase,
            CreateSerieUseCase createSerieUseCase,
            CreateTurmaUseCase createTurmaUseCase,
            LinkTurmaDisciplinaUseCase linkTurmaDisciplinaUseCase) {
        this.createPeriodoLetivoUseCase = createPeriodoLetivoUseCase;
        this.createDisciplinaUseCase = createDisciplinaUseCase;
        this.createSerieUseCase = createSerieUseCase;
        this.createTurmaUseCase = createTurmaUseCase;
        this.linkTurmaDisciplinaUseCase = linkTurmaDisciplinaUseCase;
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
}

