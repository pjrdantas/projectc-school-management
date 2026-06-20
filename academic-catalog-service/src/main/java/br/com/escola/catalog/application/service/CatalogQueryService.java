package br.com.escola.catalog.application.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import br.com.escola.catalog.application.context.InternalRequestContext;
import br.com.escola.catalog.application.dto.DisciplinaResponse;
import br.com.escola.catalog.application.dto.NivelEnsinoResponse;
import br.com.escola.catalog.application.dto.PeriodoLetivoResponse;
import br.com.escola.catalog.application.dto.SerieResponse;
import br.com.escola.catalog.application.dto.TurmaDisciplinaResponse;
import br.com.escola.catalog.application.dto.TurmaResponse;
import br.com.escola.catalog.application.dto.TurnoResponse;
import br.com.escola.catalog.application.exception.CatalogResourceNotFoundException;
import br.com.escola.catalog.application.port.in.CatalogQueryUseCase;
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
public class CatalogQueryService implements CatalogQueryUseCase {

    private final NivelEnsinoRepository nivelEnsinoRepository;
    private final PeriodoLetivoRepository periodoRepository;
    private final SerieRepository serieRepository;
    private final TurnoRepository turnoRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final TurmaRepository turmaRepository;
    private final TurmaDisciplinaRepository turmaDisciplinaRepository;

    public CatalogQueryService(
            NivelEnsinoRepository nivelEnsinoRepository,
            PeriodoLetivoRepository periodoRepository,
            SerieRepository serieRepository,
            TurnoRepository turnoRepository,
            DisciplinaRepository disciplinaRepository,
            TurmaRepository turmaRepository,
            TurmaDisciplinaRepository turmaDisciplinaRepository) {
        this.nivelEnsinoRepository = nivelEnsinoRepository;
        this.periodoRepository = periodoRepository;
        this.serieRepository = serieRepository;
        this.turnoRepository = turnoRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.turmaRepository = turmaRepository;
        this.turmaDisciplinaRepository = turmaDisciplinaRepository;
    }

    @Override
    public List<NivelEnsinoResponse> listarNiveisEnsino(InternalRequestContext context) {
        requireContext(context);
        return nivelEnsinoRepository.listarNiveisEnsino().stream().map(this::toResponse).toList();
    }

    @Override
    public List<TurnoResponse> listarTurnos(InternalRequestContext context) {
        requireContext(context);
        return turnoRepository.listarTurnos().stream().map(this::toResponse).toList();
    }

    @Override
    public TurnoResponse buscarTurno(UUID id, InternalRequestContext context) {
        requireContext(context);
        return turnoRepository.buscarTurnoPorId(id).map(this::toResponse)
                .orElseThrow(() -> notFound("Turno", id));
    }

    @Override
    public List<PeriodoLetivoResponse> listarPeriodos(InternalRequestContext context) {
        return periodoRepository.listarPeriodos(requireContext(context).escolaId()).stream()
                .map(this::toResponse).toList();
    }

    @Override
    public PeriodoLetivoResponse buscarPeriodo(UUID id, InternalRequestContext context) {
        return periodoRepository.buscarPeriodoPorId(id, requireContext(context).escolaId())
                .map(this::toResponse).orElseThrow(() -> notFound("Periodo letivo", id));
    }

    @Override
    public List<SerieResponse> listarSeries(InternalRequestContext context) {
        InternalRequestContext required = requireContext(context);
        Map<UUID, NivelEnsino> niveis = nivelEnsinoRepository.listarNiveisEnsino().stream()
                .collect(Collectors.toMap(NivelEnsino::id, Function.identity()));
        return serieRepository.listarSeries(required.escolaId()).stream()
                .map(serie -> toResponse(serie, niveis.get(serie.nivelEnsinoId()))).toList();
    }

    @Override
    public SerieResponse buscarSerie(UUID id, InternalRequestContext context) {
        Serie serie = serieRepository.buscarSeriePorId(id, requireContext(context).escolaId())
                .orElseThrow(() -> notFound("Serie", id));
        NivelEnsino nivel = nivelEnsinoRepository.buscarPorId(serie.nivelEnsinoId())
                .orElseThrow(() -> notFound("Nivel de ensino", serie.nivelEnsinoId()));
        return toResponse(serie, nivel);
    }

    @Override
    public List<TurmaResponse> listarTurmas(InternalRequestContext context) {
        InternalRequestContext required = requireContext(context);
        Map<UUID, Serie> series = serieRepository.listarSeries(required.escolaId()).stream()
                .collect(Collectors.toMap(Serie::id, Function.identity()));
        Map<UUID, Turno> turnos = turnoRepository.listarTurnos().stream()
                .collect(Collectors.toMap(Turno::id, Function.identity()));
        return turmaRepository.listarTurmas(required.escolaId()).stream()
                .map(turma -> toResponse(turma, series.get(turma.serieId()), turnos.get(turma.turnoId())))
                .toList();
    }

    @Override
    public TurmaResponse buscarTurma(UUID id, InternalRequestContext context) {
        InternalRequestContext required = requireContext(context);
        Turma turma = turmaRepository.buscarTurmaPorId(id, required.escolaId())
                .orElseThrow(() -> notFound("Turma", id));
        Serie serie = serieRepository.buscarSeriePorId(turma.serieId(), required.escolaId())
                .orElseThrow(() -> notFound("Serie", turma.serieId()));
        Turno turno = turnoRepository.buscarTurnoPorId(turma.turnoId())
                .orElseThrow(() -> notFound("Turno", turma.turnoId()));
        return toResponse(turma, serie, turno);
    }

    @Override
    public List<DisciplinaResponse> listarDisciplinas(InternalRequestContext context) {
        return disciplinaRepository.listarDisciplinas(requireContext(context).escolaId()).stream()
                .map(this::toResponse).toList();
    }

    @Override
    public DisciplinaResponse buscarDisciplina(UUID id, InternalRequestContext context) {
        return disciplinaRepository.buscarDisciplinaPorId(id, requireContext(context).escolaId())
                .map(this::toResponse).orElseThrow(() -> notFound("Disciplina", id));
    }

    @Override
    public List<TurmaDisciplinaResponse> listarDisciplinasDaTurma(
            UUID turmaId,
            InternalRequestContext context) {
        InternalRequestContext required = requireContext(context);
        turmaRepository.buscarTurmaPorId(turmaId, required.escolaId())
                .orElseThrow(() -> notFound("Turma", turmaId));
        Map<UUID, Disciplina> disciplinas = disciplinaRepository.listarDisciplinas(required.escolaId()).stream()
                .collect(Collectors.toMap(Disciplina::id, Function.identity()));
        return turmaDisciplinaRepository.listarVinculosPorTurma(turmaId, required.escolaId()).stream()
                .map(vinculo -> toResponse(vinculo, disciplinas.get(vinculo.disciplinaId())))
                .toList();
    }

    private InternalRequestContext requireContext(InternalRequestContext context) {
        return java.util.Objects.requireNonNull(context, "context nao pode ser nulo");
    }

    private CatalogResourceNotFoundException notFound(String resource, UUID id) {
        return new CatalogResourceNotFoundException(resource, id);
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
