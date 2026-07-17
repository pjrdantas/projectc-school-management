package br.com.escola.catalog.application.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import br.com.escola.catalog.application.cache.LeituraSnapshot;
import br.com.escola.catalog.application.context.InternalRequestContext;
import br.com.escola.catalog.application.dto.DisciplinaResponse;
import br.com.escola.catalog.application.dto.NivelEnsinoResponse;
import br.com.escola.catalog.application.dto.PeriodoLetivoResponse;
import br.com.escola.catalog.application.dto.SerieResponse;
import br.com.escola.catalog.application.dto.TurmaDisciplinaResponse;
import br.com.escola.catalog.application.dto.TurmaResponse;
import br.com.escola.catalog.application.dto.TurnoResponse;
import br.com.escola.catalog.application.exception.RecursoNaoEncontradoException;
import br.com.escola.catalog.application.port.in.ConsultaUseCase;
import br.com.escola.catalog.application.port.out.LeituraCachePort;
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
public class ConsultaService implements ConsultaUseCase {

    private final NivelEnsinoRepository nivelEnsinoRepository;
    private final PeriodoLetivoRepository periodoRepository;
    private final SerieRepository serieRepository;
    private final TurnoRepository turnoRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final TurmaRepository turmaRepository;
    private final TurmaDisciplinaRepository turmaDisciplinaRepository;
    private final LeituraCachePort cachePort;
    private final CacheSettings cacheSettings;

    public ConsultaService(
            NivelEnsinoRepository nivelEnsinoRepository,
            PeriodoLetivoRepository periodoRepository,
            SerieRepository serieRepository,
            TurnoRepository turnoRepository,
            DisciplinaRepository disciplinaRepository,
            TurmaRepository turmaRepository,
            TurmaDisciplinaRepository turmaDisciplinaRepository,
            LeituraCachePort cachePort,
            CacheSettings cacheSettings) {
        this.nivelEnsinoRepository = nivelEnsinoRepository;
        this.periodoRepository = periodoRepository;
        this.serieRepository = serieRepository;
        this.turnoRepository = turnoRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.turmaRepository = turmaRepository;
        this.turmaDisciplinaRepository = turmaDisciplinaRepository;
        this.cachePort = cachePort;
        this.cacheSettings = cacheSettings;
    }

    @Override
    public List<NivelEnsinoResponse> listarNiveisEnsino(InternalRequestContext context) {
        return snapshot(context).niveisEnsino();
    }

    @Override
    public List<TurnoResponse> listarTurnos(InternalRequestContext context) {
        return snapshot(context).turnos();
    }

    @Override
    public TurnoResponse buscarTurno(UUID id, InternalRequestContext context) {
        return snapshot(context).turnos().stream().filter(item -> item.id().equals(id)).findFirst()
                .orElseThrow(() -> notFound("Turno", id));
    }

    @Override
    public List<PeriodoLetivoResponse> listarPeriodos(InternalRequestContext context) {
        return snapshot(context).periodos();
    }

    @Override
    public PeriodoLetivoResponse buscarPeriodo(UUID id, InternalRequestContext context) {
        return snapshot(context).periodos().stream().filter(item -> item.id().equals(id)).findFirst()
                .orElseThrow(() -> notFound("Periodo letivo", id));
    }

    @Override
    public List<SerieResponse> listarSeries(InternalRequestContext context) {
        return snapshot(context).series();
    }

    @Override
    public SerieResponse buscarSerie(UUID id, InternalRequestContext context) {
        return snapshot(context).series().stream().filter(item -> item.id().equals(id)).findFirst()
                .orElseThrow(() -> notFound("Serie", id));
    }

    @Override
    public List<TurmaResponse> listarTurmas(InternalRequestContext context) {
        return snapshot(context).turmas();
    }

    @Override
    public TurmaResponse buscarTurma(UUID id, InternalRequestContext context) {
        return snapshot(context).turmas().stream().filter(item -> item.id().equals(id)).findFirst()
                .orElseThrow(() -> notFound("Turma", id));
    }

    @Override
    public List<DisciplinaResponse> listarDisciplinas(InternalRequestContext context) {
        return snapshot(context).disciplinas();
    }

    @Override
    public DisciplinaResponse buscarDisciplina(UUID id, InternalRequestContext context) {
        return snapshot(context).disciplinas().stream().filter(item -> item.id().equals(id)).findFirst()
                .orElseThrow(() -> notFound("Disciplina", id));
    }

    @Override
    public List<TurmaDisciplinaResponse> listarDisciplinasDaTurma(
            UUID turmaId,
            InternalRequestContext context) {
        LeituraSnapshot snapshot = snapshot(context);
        if (snapshot.turmas().stream().noneMatch(item -> item.id().equals(turmaId))) {
            throw notFound("Turma", turmaId);
        }
        return snapshot.turmaDisciplinas().stream()
                .filter(item -> item.turmaId().equals(turmaId)).toList();
    }

    private LeituraSnapshot snapshot(InternalRequestContext context) {
        InternalRequestContext required = requireContext(context);
        return cachePort.buscar(required.escolaId()).orElseGet(() -> {
            LeituraSnapshot loaded = carregarSnapshot(required);
            cachePort.armazenar(required.escolaId(), loaded, cacheSettings.ttl());
            return loaded;
        });
    }

    private LeituraSnapshot carregarSnapshot(InternalRequestContext context) {
        List<NivelEnsino> niveisDomain = nivelEnsinoRepository.listarNiveisEnsino();
        Map<UUID, NivelEnsino> niveis = niveisDomain.stream()
                .collect(Collectors.toMap(NivelEnsino::id, Function.identity()));
        List<Turno> turnosDomain = turnoRepository.listarTurnos();
        Map<UUID, Turno> turnos = turnosDomain.stream()
                .collect(Collectors.toMap(Turno::id, Function.identity()));
        List<Serie> seriesDomain = serieRepository.listarSeries(context.escolaId());
        Map<UUID, Serie> series = seriesDomain.stream()
                .collect(Collectors.toMap(Serie::id, Function.identity()));
        List<Turma> turmasDomain = turmaRepository.listarTurmas(context.escolaId());
        List<Disciplina> disciplinasDomain = disciplinaRepository.listarDisciplinas(context.escolaId());
        Map<UUID, Disciplina> disciplinas = disciplinasDomain.stream()
                .collect(Collectors.toMap(Disciplina::id, Function.identity()));
        List<TurmaDisciplinaResponse> vinculos = turmasDomain.stream()
                .flatMap(turma -> turmaDisciplinaRepository
                        .listarVinculosPorTurma(turma.id(), context.escolaId()).stream())
                .map(vinculo -> toResponse(vinculo, disciplinas.get(vinculo.disciplinaId())))
                .toList();

        return new LeituraSnapshot(
                niveisDomain.stream().map(this::toResponse).toList(),
                turnosDomain.stream().map(this::toResponse).toList(),
                periodoRepository.listarPeriodos(context.escolaId()).stream().map(this::toResponse).toList(),
                seriesDomain.stream().map(serie -> toResponse(serie, niveis.get(serie.nivelEnsinoId()))).toList(),
                turmasDomain.stream()
                        .map(turma -> toResponse(turma, series.get(turma.serieId()), turnos.get(turma.turnoId())))
                        .toList(),
                disciplinasDomain.stream().map(this::toResponse).toList(),
                vinculos);
    }

    private InternalRequestContext requireContext(InternalRequestContext context) {
        return java.util.Objects.requireNonNull(context, "context nao pode ser nulo");
    }

    private RecursoNaoEncontradoException notFound(String resource, UUID id) {
        return new RecursoNaoEncontradoException(resource, id);
    }

    private NivelEnsinoResponse toResponse(NivelEnsino nivel) {
        return new NivelEnsinoResponse(nivel.id(), nivel.codigo(), nivel.descricao());
    }

    private TurnoResponse toResponse(Turno turno) {
        return new TurnoResponse(turno.id(), turno.codigo(), turno.descricao());
    }

    private PeriodoLetivoResponse toResponse(PeriodoLetivo periodo) {
        return new PeriodoLetivoResponse(
                periodo.id(), periodo.nome(), periodo.ano(), periodo.dataInicio(), periodo.dataFim(),
                periodo.ativo(), periodo.escolaId().value(), periodo.createdAt());
    }

    private SerieResponse toResponse(Serie serie, NivelEnsino nivel) {
        return new SerieResponse(
                serie.id(), serie.nome(), serie.ordem(), serie.nivelEnsinoId(),
                nivel == null ? null : nivel.codigo(), serie.escolaId().value(), serie.createdAt());
    }

    private DisciplinaResponse toResponse(Disciplina disciplina) {
        return new DisciplinaResponse(
                disciplina.id(), disciplina.nome(), disciplina.cargaHoraria(), disciplina.ativo(),
                disciplina.escolaId().value(), disciplina.createdAt());
    }

    private TurmaResponse toResponse(Turma turma, Serie serie, Turno turno) {
        return new TurmaResponse(
                turma.id(), turma.codigo(), turma.nome(), turma.capacidade(), turma.periodoLetivoId(),
                turma.serieId(), serie == null ? null : serie.nome(), turma.turnoId(),
                turno == null ? null : turno.codigo(), turma.ativo(), turma.escolaId().value(), turma.createdAt());
    }

    private TurmaDisciplinaResponse toResponse(TurmaDisciplina vinculo, Disciplina disciplina) {
        return new TurmaDisciplinaResponse(
                vinculo.id(), vinculo.turmaId(), vinculo.disciplinaId(),
                disciplina == null ? null : disciplina.nome(), vinculo.cargaHoraria(),
                vinculo.escolaId().value(), vinculo.createdAt());
    }
}

