package br.com.escola.catalog.infra.database.adapter;

import static br.com.escola.catalog.infra.database.mapper.CatalogPersistenceMapper.toDomain;
import static br.com.escola.catalog.infra.database.mapper.CatalogPersistenceMapper.toEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
import br.com.escola.catalog.domain.valueobject.EscolaId;
import br.com.escola.catalog.infra.database.mapper.CatalogPersistenceMapper;
import br.com.escola.catalog.infra.database.repository.DisciplinaJpaRepository;
import br.com.escola.catalog.infra.database.repository.NivelEnsinoJpaRepository;
import br.com.escola.catalog.infra.database.repository.PeriodoLetivoJpaRepository;
import br.com.escola.catalog.infra.database.repository.SerieJpaRepository;
import br.com.escola.catalog.infra.database.repository.TurmaDisciplinaJpaRepository;
import br.com.escola.catalog.infra.database.repository.TurmaJpaRepository;
import br.com.escola.catalog.infra.database.repository.TurnoJpaRepository;

@Repository
@Transactional
public class CatalogPersistenceAdapter implements
        NivelEnsinoRepository,
        PeriodoLetivoRepository,
        SerieRepository,
        TurnoRepository,
        DisciplinaRepository,
        TurmaRepository,
        TurmaDisciplinaRepository {

    private final NivelEnsinoJpaRepository nivelEnsinoRepository;
    private final PeriodoLetivoJpaRepository periodoRepository;
    private final SerieJpaRepository serieRepository;
    private final TurnoJpaRepository turnoRepository;
    private final DisciplinaJpaRepository disciplinaRepository;
    private final TurmaJpaRepository turmaRepository;
    private final TurmaDisciplinaJpaRepository turmaDisciplinaRepository;

    public CatalogPersistenceAdapter(
            NivelEnsinoJpaRepository nivelEnsinoRepository,
            PeriodoLetivoJpaRepository periodoRepository,
            SerieJpaRepository serieRepository,
            TurnoJpaRepository turnoRepository,
            DisciplinaJpaRepository disciplinaRepository,
            TurmaJpaRepository turmaRepository,
            TurmaDisciplinaJpaRepository turmaDisciplinaRepository) {
        this.nivelEnsinoRepository = nivelEnsinoRepository;
        this.periodoRepository = periodoRepository;
        this.serieRepository = serieRepository;
        this.turnoRepository = turnoRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.turmaRepository = turmaRepository;
        this.turmaDisciplinaRepository = turmaDisciplinaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NivelEnsino> buscarPorId(UUID id) {
        return nivelEnsinoRepository.findById(id).map(CatalogPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NivelEnsino> listarNiveisEnsino() {
        return nivelEnsinoRepository.findAllByOrderByCodigoAsc().stream()
                .map(CatalogPersistenceMapper::toDomain).toList();
    }

    @Override
    public PeriodoLetivo salvar(PeriodoLetivo periodo) {
        return toDomain(periodoRepository.save(toEntity(periodo)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PeriodoLetivo> buscarPeriodoPorId(UUID id, EscolaId escolaId) {
        return periodoRepository.findByIdAndEscolaId(id, escolaId.value()).map(CatalogPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PeriodoLetivo> listarPeriodos(EscolaId escolaId) {
        return periodoRepository.findAllByEscolaIdOrderByAnoDescNomeAsc(escolaId.value()).stream()
                .map(CatalogPersistenceMapper::toDomain).toList();
    }

    @Override
    public Serie salvar(Serie serie) {
        return toDomain(serieRepository.save(toEntity(serie)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Serie> buscarSeriePorId(UUID id, EscolaId escolaId) {
        return serieRepository.findByIdAndEscolaId(id, escolaId.value()).map(CatalogPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Serie> listarSeries(EscolaId escolaId) {
        return serieRepository.findAllByEscolaIdOrderByOrdemAscNomeAsc(escolaId.value()).stream()
                .map(CatalogPersistenceMapper::toDomain).toList();
    }

    @Override
    public Turno salvar(Turno turno) {
        return toDomain(turnoRepository.save(toEntity(turno)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Turno> buscarTurnoPorId(UUID id) {
        return turnoRepository.findById(id).map(CatalogPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Turno> buscarTurnoPorCodigo(String codigo) {
        return turnoRepository.findByCodigoIgnoreCase(codigo).map(CatalogPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Turno> listarTurnos() {
        return turnoRepository.findAllByOrderByCodigoAsc().stream()
                .map(CatalogPersistenceMapper::toDomain).toList();
    }

    @Override
    public Disciplina salvar(Disciplina disciplina) {
        return toDomain(disciplinaRepository.save(toEntity(disciplina)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Disciplina> buscarDisciplinaPorId(UUID id, EscolaId escolaId) {
        return disciplinaRepository.findByIdAndEscolaId(id, escolaId.value()).map(CatalogPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Disciplina> listarDisciplinas(EscolaId escolaId) {
        return disciplinaRepository.findAllByEscolaIdOrderByNomeAsc(escolaId.value()).stream()
                .map(CatalogPersistenceMapper::toDomain).toList();
    }

    @Override
    public Turma salvar(Turma turma) {
        return toDomain(turmaRepository.save(toEntity(turma)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Turma> buscarTurmaPorId(UUID id, EscolaId escolaId) {
        return turmaRepository.findByIdAndEscolaId(id, escolaId.value()).map(CatalogPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Turma> listarTurmas(EscolaId escolaId) {
        return turmaRepository.findAllByEscolaIdOrderByCodigoAsc(escolaId.value()).stream()
                .map(CatalogPersistenceMapper::toDomain).toList();
    }

    @Override
    public TurmaDisciplina salvar(TurmaDisciplina vinculo) {
        return toDomain(turmaDisciplinaRepository.save(toEntity(vinculo)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TurmaDisciplina> buscarVinculoPorId(UUID id, EscolaId escolaId) {
        return turmaDisciplinaRepository.findByIdAndEscolaId(id, escolaId.value())
                .map(CatalogPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TurmaDisciplina> listarVinculosPorTurma(UUID turmaId, EscolaId escolaId) {
        return turmaDisciplinaRepository.findAllByTurmaIdAndEscolaId(turmaId, escolaId.value()).stream()
                .map(CatalogPersistenceMapper::toDomain).toList();
    }
}
