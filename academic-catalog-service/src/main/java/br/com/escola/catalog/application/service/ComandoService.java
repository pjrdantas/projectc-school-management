package br.com.escola.catalog.application.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import br.com.escola.catalog.application.command.CommandResult;
import br.com.escola.catalog.application.command.CreateDisciplinaCommand;
import br.com.escola.catalog.application.command.CreatePeriodoLetivoCommand;
import br.com.escola.catalog.application.command.CreateSerieCommand;
import br.com.escola.catalog.application.command.CreateTurmaCommand;
import br.com.escola.catalog.application.command.CreateTurnoCommand;
import br.com.escola.catalog.application.command.LinkDisciplinaCommand;
import br.com.escola.catalog.application.command.UpdateDisciplinaCommand;
import br.com.escola.catalog.application.command.UpdatePeriodoLetivoCommand;
import br.com.escola.catalog.application.command.UpdateSerieCommand;
import br.com.escola.catalog.application.command.UpdateTurmaCommand;
import br.com.escola.catalog.application.command.UpdateTurnoCommand;
import br.com.escola.catalog.application.command.UpdateTurmaDisciplinaCommand;
import br.com.escola.catalog.application.context.InternalRequestContext;
import br.com.escola.catalog.application.dto.DisciplinaResponse;
import br.com.escola.catalog.application.dto.PeriodoLetivoResponse;
import br.com.escola.catalog.application.dto.SerieResponse;
import br.com.escola.catalog.application.dto.TurmaDisciplinaResponse;
import br.com.escola.catalog.application.dto.TurmaResponse;
import br.com.escola.catalog.application.dto.TurnoResponse;
import br.com.escola.catalog.application.event.IntegrationEventEnvelope;
import br.com.escola.catalog.application.event.OutboxEvent;
import br.com.escola.catalog.application.exception.RecursoNaoEncontradoException;
import br.com.escola.catalog.application.exception.ConflitoNegocioException;
import br.com.escola.catalog.application.exception.IdempotencyConflictException;
import br.com.escola.catalog.application.idempotency.CommandIdempotency;
import br.com.escola.catalog.application.port.in.ComandoUseCase;
import br.com.escola.catalog.application.port.out.LeituraCachePort;
import br.com.escola.catalog.application.port.out.IdempotencyPort;
import br.com.escola.catalog.application.port.out.IntegrationEventOutboxPort;
import br.com.escola.catalog.domain.model.Disciplina;
import br.com.escola.catalog.domain.model.NivelEnsino;
import br.com.escola.catalog.domain.model.PeriodoLetivo;
import br.com.escola.catalog.domain.model.Serie;
import br.com.escola.catalog.domain.model.Turma;
import br.com.escola.catalog.domain.model.TurmaDisciplina;
import br.com.escola.catalog.domain.model.Turno;
import br.com.escola.catalog.domain.repository.DisciplinaRepository;
import br.com.escola.catalog.domain.repository.NivelEnsinoRepository;
import br.com.escola.catalog.domain.repository.PeriodoLetivoRepository;
import br.com.escola.catalog.domain.repository.SerieRepository;
import br.com.escola.catalog.domain.repository.TurmaDisciplinaRepository;
import br.com.escola.catalog.domain.repository.TurmaRepository;
import br.com.escola.catalog.domain.repository.TurnoRepository;

@Service
public class ComandoService implements ComandoUseCase {

    private final NivelEnsinoRepository nivelEnsinoRepository;
    private final PeriodoLetivoRepository periodoRepository;
    private final SerieRepository serieRepository;
    private final TurnoRepository turnoRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final TurmaRepository turmaRepository;
    private final TurmaDisciplinaRepository turmaDisciplinaRepository;
    private final IdempotencyPort idempotencyPort;
    private final IntegrationEventOutboxPort outboxPort;
    private final LeituraCachePort cachePort;

    public ComandoService(
            NivelEnsinoRepository nivelEnsinoRepository,
            PeriodoLetivoRepository periodoRepository,
            SerieRepository serieRepository,
            TurnoRepository turnoRepository,
            DisciplinaRepository disciplinaRepository,
            TurmaRepository turmaRepository,
            TurmaDisciplinaRepository turmaDisciplinaRepository,
            IdempotencyPort idempotencyPort,
            IntegrationEventOutboxPort outboxPort,
            LeituraCachePort cachePort) {
        this.nivelEnsinoRepository = nivelEnsinoRepository;
        this.periodoRepository = periodoRepository;
        this.serieRepository = serieRepository;
        this.turnoRepository = turnoRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.turmaRepository = turmaRepository;
        this.turmaDisciplinaRepository = turmaDisciplinaRepository;
        this.idempotencyPort = idempotencyPort;
        this.outboxPort = outboxPort;
        this.cachePort = cachePort;
    }

    @Override
    @Transactional
    public CommandResult<TurnoResponse> criarTurno(CreateTurnoCommand command, String idempotencyKey, InternalRequestContext context) {
        String fingerprint = CommandFingerprint.sha256("CREATE_TURNO", command.codigo(), command.descricao());
        return execute(idempotencyKey, fingerprint, "TURNO", context,
                id -> turnoRepository.buscarTurnoPorId(id).map(this::toResponse).orElseThrow(() -> notFound("Turno", id)),
                () -> {
                    if (turnoRepository.buscarTurnoPorCodigo(command.codigo()).isPresent()) throw new ConflitoNegocioException("Codigo de turno ja existe");
                    Turno turno = turnoRepository.salvar(new Turno(UUID.randomUUID(), command.codigo(), command.descricao()));
                    return created(turno.id(), toResponse(turno), "TURNO", "shift-created", context, payload("turnoId", turno.id()));
                });
    }

    @Override
    @Transactional
    public CommandResult<TurnoResponse> atualizarTurno(UUID turnoId, UpdateTurnoCommand command, String idempotencyKey, InternalRequestContext context) {
        String fingerprint = CommandFingerprint.sha256("UPDATE_TURNO", turnoId, command.codigo(), command.descricao());
        return execute(idempotencyKey, fingerprint, "TURNO", context,
                id -> turnoRepository.buscarTurnoPorId(id).map(this::toResponse).orElseThrow(() -> notFound("Turno", id)),
                () -> {
                    Turno atual = turno(turnoId);
                    turnoRepository.buscarTurnoPorCodigo(command.codigo()).filter(outro -> !outro.id().equals(turnoId)).ifPresent(outro -> { throw new ConflitoNegocioException("Codigo de turno ja existe"); });
                    Turno atualizado = turnoRepository.salvar(new Turno(atual.id(), command.codigo(), command.descricao()));
                    return created(atualizado.id(), toResponse(atualizado), "TURNO", "shift-updated", context, payload("turnoId", atualizado.id()));
                });
    }

    @Override
    @Transactional
    public CommandResult<PeriodoLetivoResponse> criarPeriodo(
            CreatePeriodoLetivoCommand command,
            String idempotencyKey,
            InternalRequestContext context) {
        String fingerprint = CommandFingerprint.sha256(
                "CREATE_PERIODO", command.nome(), command.ano(), command.dataInicio(), command.dataFim());
        return execute(idempotencyKey, fingerprint, "PERIODO_LETIVO", context,
                id -> periodoRepository.buscarPeriodoPorId(id, context.escolaId())
                        .map(this::toResponse).orElseThrow(() -> notFound("Periodo letivo", id)),
                () -> {
                    LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
                    PeriodoLetivo periodo = periodoRepository.salvar(new PeriodoLetivo(
                            UUID.randomUUID(), context.escolaId(), command.nome(), command.ano(),
                            command.dataInicio(), command.dataFim(), true, now));
                    return created(periodo.id(), toResponse(periodo), "PERIODO_LETIVO", "term-created", context,
                            payload("periodoLetivoId", periodo.id(), "nome", periodo.nome()));
                });
    }

    @Override
    @Transactional
    public CommandResult<PeriodoLetivoResponse> atualizarPeriodo(
            UUID periodoId,
            UpdatePeriodoLetivoCommand command,
            String idempotencyKey,
            InternalRequestContext context) {
        PeriodoLetivo existente = periodo(periodoId, context);
        String fingerprint = CommandFingerprint.sha256(
                "UPDATE_PERIODO", periodoId, command.nome(), command.ano(), command.dataInicio(), command.dataFim(), command.ativo());
        return execute(idempotencyKey, fingerprint, "PERIODO_LETIVO", context,
                id -> periodoRepository.buscarPeriodoPorId(id, context.escolaId())
                        .map(this::toResponse).orElseThrow(() -> notFound("Periodo letivo", id)),
                () -> {
                    PeriodoLetivo atualizado = periodoRepository.salvar(new PeriodoLetivo(
                            existente.id(), existente.escolaId(), command.nome(), command.ano(),
                            command.dataInicio(), command.dataFim(), command.ativo(), existente.createdAt()));
                    return created(atualizado.id(), toResponse(atualizado), "PERIODO_LETIVO", "term-updated", context,
                            payload("periodoLetivoId", atualizado.id(), "nome", atualizado.nome()));
                });
    }

    @Override
    @Transactional
    public CommandResult<Void> excluirPeriodo(
            UUID periodoId,
            String idempotencyKey,
            InternalRequestContext context) {
        String fingerprint = CommandFingerprint.sha256("DELETE_PERIODO", periodoId);
        return execute(idempotencyKey, fingerprint, "PERIODO_LETIVO", context,
                ignored -> null,
                () -> {
                    periodo(periodoId, context);
                    boolean possuiTurmas = turmaRepository.listarTurmas(context.escolaId()).stream()
                            .anyMatch(turma -> turma.periodoLetivoId().equals(periodoId));
                    if (possuiTurmas) {
                        throw new ConflitoNegocioException("Periodo letivo possui turmas vinculadas");
                    }
                    periodoRepository.excluir(periodoId, context.escolaId());
                    return created(periodoId, null, "PERIODO_LETIVO", "term-deleted", context,
                            payload("periodoLetivoId", periodoId));
                });
    }

    @Override
    @Transactional
    public CommandResult<SerieResponse> criarSerie(
            CreateSerieCommand command,
            String idempotencyKey,
            InternalRequestContext context) {
        String fingerprint = CommandFingerprint.sha256(
                "CREATE_SERIE", command.nome(), command.ordem(), command.nivelEnsinoId());
        return execute(idempotencyKey, fingerprint, "SERIE", context,
                id -> {
                    Serie serie = serieRepository.buscarSeriePorId(id, context.escolaId())
                            .orElseThrow(() -> notFound("Serie", id));
                    return toResponse(serie, nivel(command.nivelEnsinoId()));
                },
                () -> {
                    NivelEnsino nivel = nivel(command.nivelEnsinoId());
                    Serie serie = serieRepository.salvar(new Serie(
                            UUID.randomUUID(), context.escolaId(), command.nome(), command.ordem(),
                            nivel.id(), LocalDateTime.now(ZoneOffset.UTC)));
                    return created(serie.id(), toResponse(serie, nivel), "SERIE", "grade-created", context,
                            payload("serieId", serie.id(), "nivelEnsinoId", nivel.id()));
                });
    }

    @Override
    @Transactional
    public CommandResult<DisciplinaResponse> criarDisciplina(
            CreateDisciplinaCommand command,
            String idempotencyKey,
            InternalRequestContext context) {
        String fingerprint = CommandFingerprint.sha256(
                "CREATE_DISCIPLINA", command.nome(), command.cargaHoraria(), command.ativo());
        return execute(idempotencyKey, fingerprint, "DISCIPLINA", context,
                id -> disciplinaRepository.buscarDisciplinaPorId(id, context.escolaId())
                        .map(this::toResponse).orElseThrow(() -> notFound("Disciplina", id)),
                () -> {
                    boolean ativo = command.ativo() == null ? true : command.ativo();
                    Disciplina disciplina = disciplinaRepository.salvar(new Disciplina(
                            UUID.randomUUID(), context.escolaId(), command.nome(), command.cargaHoraria(),
                            ativo, LocalDateTime.now(ZoneOffset.UTC)));
                    return created(disciplina.id(), toResponse(disciplina),
                            "DISCIPLINA", "subject-created", context,
                            payload("disciplinaId", disciplina.id(), "nome", disciplina.nome()));
                });
    }

    @Override
    @Transactional
    public CommandResult<SerieResponse> atualizarSerie(
            UUID serieId,
            UpdateSerieCommand command,
            String idempotencyKey,
            InternalRequestContext context) {
        Serie existente = serie(serieId, context);
        NivelEnsino nivel = nivel(command.nivelEnsinoId());
        String fingerprint = CommandFingerprint.sha256(
                "UPDATE_SERIE", serieId, command.nome(), command.ordem(), command.nivelEnsinoId());
        return execute(idempotencyKey, fingerprint, "SERIE", context,
                id -> serieRepository.buscarSeriePorId(id, context.escolaId())
                        .map(serie -> toResponse(serie, nivel(serie.nivelEnsinoId())))
                        .orElseThrow(() -> notFound("Serie", id)),
                () -> {
                    Serie atualizada = serieRepository.salvar(new Serie(
                            existente.id(), existente.escolaId(), command.nome(), command.ordem(),
                            nivel.id(), existente.createdAt()));
                    return created(atualizada.id(), toResponse(atualizada, nivel), "SERIE", "grade-updated", context,
                            payload("serieId", atualizada.id(), "nivelEnsinoId", nivel.id()));
                });
    }

    @Override
    @Transactional
    public CommandResult<Void> excluirSerie(
            UUID serieId,
            String idempotencyKey,
            InternalRequestContext context) {
        String fingerprint = CommandFingerprint.sha256("DELETE_SERIE", serieId);
        return execute(idempotencyKey, fingerprint, "SERIE", context,
                ignored -> null,
                () -> {
                    serie(serieId, context);
                    boolean possuiTurmas = turmaRepository.listarTurmas(context.escolaId()).stream()
                            .anyMatch(turma -> turma.serieId().equals(serieId));
                    if (possuiTurmas) {
                        throw new ConflitoNegocioException("Serie possui turmas vinculadas");
                    }
                    serieRepository.excluirSerie(serieId, context.escolaId());
                    return created(serieId, null, "SERIE", "grade-deleted", context,
                            payload("serieId", serieId));
                });
    }

    @Override
    @Transactional
    public CommandResult<DisciplinaResponse> atualizarDisciplina(
            UUID disciplinaId,
            UpdateDisciplinaCommand command,
            String idempotencyKey,
            InternalRequestContext context) {
        Disciplina existente = disciplina(disciplinaId, context);
        String fingerprint = CommandFingerprint.sha256(
                "UPDATE_DISCIPLINA", disciplinaId, command.nome(), command.cargaHoraria(), command.ativo());
        return execute(idempotencyKey, fingerprint, "DISCIPLINA", context,
                id -> disciplinaRepository.buscarDisciplinaPorId(id, context.escolaId())
                        .map(this::toResponse).orElseThrow(() -> notFound("Disciplina", id)),
                () -> {
                    Disciplina atualizada = disciplinaRepository.salvar(new Disciplina(
                            existente.id(), existente.escolaId(), command.nome(), command.cargaHoraria(),
                            command.ativo(), existente.createdAt()));
                    return created(atualizada.id(), toResponse(atualizada),
                            "DISCIPLINA", "subject-updated", context,
                            payload("disciplinaId", atualizada.id(), "nome", atualizada.nome()));
                });
    }

    @Override
    @Transactional
    public CommandResult<Void> excluirDisciplina(
            UUID disciplinaId,
            String idempotencyKey,
            InternalRequestContext context) {
        String fingerprint = CommandFingerprint.sha256("DELETE_DISCIPLINA", disciplinaId);
        return execute(idempotencyKey, fingerprint, "DISCIPLINA", context,
                ignored -> null,
                () -> {
                    disciplina(disciplinaId, context);
                    if (turmaDisciplinaRepository.possuiVinculoComDisciplina(disciplinaId, context.escolaId())) {
                        throw new ConflitoNegocioException("Disciplina possui vinculos com turmas");
                    }
                    disciplinaRepository.excluirDisciplina(disciplinaId, context.escolaId());
                    return created(disciplinaId, null, "DISCIPLINA", "subject-deleted", context,
                            payload("disciplinaId", disciplinaId));
                });
    }

    @Override
    @Transactional
    public CommandResult<TurmaResponse> criarTurma(
            CreateTurmaCommand command,
            String idempotencyKey,
            InternalRequestContext context) {
        String fingerprint = CommandFingerprint.sha256(
                "CREATE_TURMA", command.codigo(), command.nome(), command.capacidade(),
                command.periodoLetivoId(), command.serieId(), command.turnoId());
        return execute(idempotencyKey, fingerprint, "TURMA", context,
                id -> {
                    Turma turma = turmaRepository.buscarTurmaPorId(id, context.escolaId())
                            .orElseThrow(() -> notFound("Turma", id));
                    return toResponse(turma, serie(turma.serieId(), context), turno(turma.turnoId()));
                },
                () -> {
                    periodo(command.periodoLetivoId(), context);
                    Serie serie = serie(command.serieId(), context);
                    Turno turno = turno(command.turnoId());
                    Turma turma = turmaRepository.salvar(new Turma(
                            UUID.randomUUID(), context.escolaId(), command.codigo(), command.nome(),
                            command.capacidade(), command.periodoLetivoId(), serie.id(), turno.id(), true,
                            LocalDateTime.now(ZoneOffset.UTC)));
                    return created(turma.id(), toResponse(turma, serie, turno), "TURMA", "class-created", context,
                            payload("turmaId", turma.id(), "periodoLetivoId", turma.periodoLetivoId()));
                });
    }

    @Override
    @Transactional
    public CommandResult<TurmaResponse> atualizarTurma(
            UUID turmaId,
            UpdateTurmaCommand command,
            String idempotencyKey,
            InternalRequestContext context) {
        Turma existente = turma(turmaId, context);
        PeriodoLetivo periodo = periodo(command.periodoLetivoId(), context);
        Serie serie = serie(command.serieId(), context);
        Turno turno = turno(command.turnoId());
        String fingerprint = CommandFingerprint.sha256(
                "UPDATE_TURMA", turmaId, command.codigo(), command.nome(), command.capacidade(),
                command.periodoLetivoId(), command.serieId(), command.turnoId(), command.ativo());
        return execute(idempotencyKey, fingerprint, "TURMA", context,
                id -> {
                    Turma turma = turmaRepository.buscarTurmaPorId(id, context.escolaId())
                            .orElseThrow(() -> notFound("Turma", id));
                    return toResponse(turma, serie(turma.serieId(), context), turno(turma.turnoId()));
                },
                () -> {
                    Turma atualizada = turmaRepository.salvar(new Turma(
                            existente.id(), existente.escolaId(), command.codigo(), command.nome(), command.capacidade(),
                            periodo.id(), serie.id(), turno.id(), command.ativo(), existente.createdAt()));
                    return created(atualizada.id(), toResponse(atualizada, serie, turno), "TURMA", "class-updated", context,
                            payload("turmaId", atualizada.id(), "periodoLetivoId", periodo.id(), "serieId", serie.id()));
                });
    }

    @Override
    @Transactional
    public CommandResult<Void> excluirTurma(
            UUID turmaId,
            String idempotencyKey,
            InternalRequestContext context) {
        String fingerprint = CommandFingerprint.sha256("DELETE_TURMA", turmaId);
        return execute(idempotencyKey, fingerprint, "TURMA", context,
                ignored -> null,
                () -> {
                    turma(turmaId, context);
                    if (!turmaDisciplinaRepository.listarVinculosPorTurma(turmaId, context.escolaId()).isEmpty()) {
                        throw new ConflitoNegocioException("Turma possui disciplinas vinculadas");
                    }
                    turmaRepository.excluirTurma(turmaId, context.escolaId());
                    return created(turmaId, null, "TURMA", "class-deleted", context,
                            payload("turmaId", turmaId));
                });
    }

    @Override
    @Transactional
    public CommandResult<TurmaDisciplinaResponse> vincularDisciplina(
            UUID turmaId,
            LinkDisciplinaCommand command,
            String idempotencyKey,
            InternalRequestContext context) {
        String fingerprint = CommandFingerprint.sha256(
                "LINK_DISCIPLINA", turmaId, command.disciplinaId(), command.cargaHoraria());
        return execute(idempotencyKey, fingerprint, "TURMA_DISCIPLINA", context,
                id -> {
                    TurmaDisciplina vinculo = turmaDisciplinaRepository.buscarVinculoPorId(id, context.escolaId())
                            .orElseThrow(() -> notFound("Vinculo turma-disciplina", id));
                    Disciplina disciplina = disciplina(vinculo.disciplinaId(), context);
                    return toResponse(vinculo, disciplina);
                },
                () -> {
                    turma(turmaId, context);
                    Disciplina disciplina = disciplina(command.disciplinaId(), context);
                    TurmaDisciplina vinculo = turmaDisciplinaRepository.salvar(new TurmaDisciplina(
                            UUID.randomUUID(), context.escolaId(), turmaId, disciplina.id(),
                            command.cargaHoraria(), LocalDateTime.now(ZoneOffset.UTC)));
                    return created(vinculo.id(), toResponse(vinculo, disciplina),
                            "TURMA_DISCIPLINA", "class-subject-created", context,
                            payload("turmaDisciplinaId", vinculo.id(), "turmaId", turmaId,
                                    "disciplinaId", disciplina.id()));
                });
    }

    @Override
    @Transactional
    public CommandResult<TurmaDisciplinaResponse> atualizarVinculoDisciplina(
            UUID turmaId,
            UUID vinculoId,
            UpdateTurmaDisciplinaCommand command,
            String idempotencyKey,
            InternalRequestContext context) {
        turma(turmaId, context);
        TurmaDisciplina existente = vinculoDaTurma(vinculoId, turmaId, context);
        Disciplina disciplina = disciplina(existente.disciplinaId(), context);
        String fingerprint = CommandFingerprint.sha256("UPDATE_TURMA_DISCIPLINA", turmaId, vinculoId, command.cargaHoraria());
        return execute(idempotencyKey, fingerprint, "TURMA_DISCIPLINA", context,
                id -> turmaDisciplinaRepository.buscarVinculoPorId(id, context.escolaId())
                        .map(vinculo -> toResponse(vinculo, disciplina(vinculo.disciplinaId(), context)))
                        .orElseThrow(() -> notFound("Vinculo turma-disciplina", id)),
                () -> {
                    TurmaDisciplina atualizado = turmaDisciplinaRepository.salvar(new TurmaDisciplina(
                            existente.id(), existente.escolaId(), existente.turmaId(), existente.disciplinaId(),
                            command.cargaHoraria(), existente.createdAt()));
                    return created(atualizado.id(), toResponse(atualizado, disciplina),
                            "TURMA_DISCIPLINA", "class-subject-updated", context,
                            payload("turmaDisciplinaId", atualizado.id(), "turmaId", turmaId,
                                    "disciplinaId", disciplina.id()));
                });
    }

    @Override
    @Transactional
    public CommandResult<Void> desvincularDisciplina(
            UUID turmaId,
            UUID vinculoId,
            String idempotencyKey,
            InternalRequestContext context) {
        String fingerprint = CommandFingerprint.sha256("UNLINK_DISCIPLINA", turmaId, vinculoId);
        return execute(idempotencyKey, fingerprint, "TURMA_DISCIPLINA", context,
                ignored -> null,
                () -> {
                    turma(turmaId, context);
                    TurmaDisciplina vinculo = vinculoDaTurma(vinculoId, turmaId, context);
                    turmaDisciplinaRepository.excluirVinculo(vinculo.id(), context.escolaId());
                    return created(vinculo.id(), null, "TURMA_DISCIPLINA", "class-subject-deleted", context,
                            payload("turmaDisciplinaId", vinculo.id(), "turmaId", turmaId,
                                    "disciplinaId", vinculo.disciplinaId()));
                });
    }

    private <T> CommandResult<T> execute(
            String key,
            String fingerprint,
            String resourceType,
            InternalRequestContext context,
            Function<UUID, T> replayLoader,
            Supplier<CreatedResource<T>> creator) {
        String validKey = validKey(key);
        UUID escolaId = context.escolaId().value();
        idempotencyPort.bloquear(escolaId, validKey);
        var existing = idempotencyPort.buscar(escolaId, validKey);
        if (existing.isPresent()) {
            CommandIdempotency saved = existing.get();
            if (!saved.requestHash().equals(fingerprint) || !saved.resourceType().equals(resourceType)) {
                throw new IdempotencyConflictException(validKey);
            }
            return new CommandResult<>(replayLoader.apply(saved.resourceId()), true);
        }

        CreatedResource<T> created = creator.get();
        outboxPort.adicionar(created.event());
        idempotencyPort.salvar(new CommandIdempotency(
                escolaId, validKey, fingerprint, resourceType, created.resourceId(), Instant.now()));
        invalidarCacheAposCommit(context);
        return new CommandResult<>(created.response(), false);
    }

    private void invalidarCacheAposCommit(InternalRequestContext context) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            cachePort.invalidar(context.escolaId());
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                cachePort.invalidar(context.escolaId());
            }
        });
    }

    private <T> CreatedResource<T> created(
            UUID resourceId,
            T response,
            String aggregateType,
            String eventType,
            InternalRequestContext context,
            Map<String, Object> payload) {
        Instant occurredAt = Instant.now();
        IntegrationEventEnvelope envelope = new IntegrationEventEnvelope(
                UUID.randomUUID(), eventType, 1, occurredAt, context.correlationId(), null,
                context.usuarioId(), context.escolaId().value(), payload);
        return new CreatedResource<>(resourceId, response, new OutboxEvent(aggregateType, resourceId, envelope));
    }

    private String validKey(String key) {
        if (key == null || key.isBlank() || key.length() > 160) {
            throw new IllegalArgumentException("Idempotency-Key deve conter entre 1 e 160 caracteres");
        }
        return key.trim();
    }

    private NivelEnsino nivel(UUID id) {
        return nivelEnsinoRepository.buscarPorId(id).orElseThrow(() -> notFound("Nivel de ensino", id));
    }

    private PeriodoLetivo periodo(UUID id, InternalRequestContext context) {
        return periodoRepository.buscarPeriodoPorId(id, context.escolaId())
                .orElseThrow(() -> notFound("Periodo letivo", id));
    }

    private Serie serie(UUID id, InternalRequestContext context) {
        return serieRepository.buscarSeriePorId(id, context.escolaId())
                .orElseThrow(() -> notFound("Serie", id));
    }

    private Turno turno(UUID id) {
        return turnoRepository.buscarTurnoPorId(id).orElseThrow(() -> notFound("Turno", id));
    }

    private Turma turma(UUID id, InternalRequestContext context) {
        return turmaRepository.buscarTurmaPorId(id, context.escolaId())
                .orElseThrow(() -> notFound("Turma", id));
    }

    private Disciplina disciplina(UUID id, InternalRequestContext context) {
        return disciplinaRepository.buscarDisciplinaPorId(id, context.escolaId())
                .orElseThrow(() -> notFound("Disciplina", id));
    }

    private TurmaDisciplina vinculoDaTurma(UUID vinculoId, UUID turmaId, InternalRequestContext context) {
        TurmaDisciplina vinculo = turmaDisciplinaRepository.buscarVinculoPorId(vinculoId, context.escolaId())
                .orElseThrow(() -> notFound("Vinculo turma-disciplina", vinculoId));
        if (!vinculo.turmaId().equals(turmaId)) {
            throw new RecursoNaoEncontradoException("Vinculo turma-disciplina", vinculoId);
        }
        return vinculo;
    }

    private RecursoNaoEncontradoException notFound(String resource, UUID id) {
        return new RecursoNaoEncontradoException(resource, id);
    }

    private Map<String, Object> payload(Object... values) {
        Map<String, Object> payload = new HashMap<>();
        for (int index = 0; index < values.length; index += 2) {
            payload.put(values[index].toString(), values[index + 1]);
        }
        return payload;
    }

    private PeriodoLetivoResponse toResponse(PeriodoLetivo periodo) {
        return new PeriodoLetivoResponse(
                periodo.id(), periodo.nome(), periodo.ano(), periodo.dataInicio(), periodo.dataFim(),
                periodo.ativo(), periodo.escolaId().value(), periodo.createdAt());
    }

    private TurnoResponse toResponse(Turno turno) { return new TurnoResponse(turno.id(), turno.codigo(), turno.descricao()); }

    private SerieResponse toResponse(Serie serie, NivelEnsino nivel) {
        return new SerieResponse(
                serie.id(), serie.nome(), serie.ordem(), nivel.id(), nivel.codigo(),
                serie.escolaId().value(), serie.createdAt());
    }

    private DisciplinaResponse toResponse(Disciplina disciplina) {
        return new DisciplinaResponse(
                disciplina.id(), disciplina.nome(), disciplina.cargaHoraria(), disciplina.ativo(),
                disciplina.escolaId().value(), disciplina.createdAt());
    }

    private TurmaResponse toResponse(Turma turma, Serie serie, Turno turno) {
        return new TurmaResponse(
                turma.id(), turma.codigo(), turma.nome(), turma.capacidade(), turma.periodoLetivoId(),
                serie.id(), serie.nome(), turno.id(), turno.codigo(), turma.ativo(),
                turma.escolaId().value(), turma.createdAt());
    }

    private TurmaDisciplinaResponse toResponse(TurmaDisciplina vinculo, Disciplina disciplina) {
        return new TurmaDisciplinaResponse(
                vinculo.id(), vinculo.turmaId(), disciplina.id(), disciplina.nome(),
                vinculo.cargaHoraria(), vinculo.escolaId().value(), vinculo.createdAt());
    }

    private record CreatedResource<T>(UUID resourceId, T response, OutboxEvent event) {
    }
}

