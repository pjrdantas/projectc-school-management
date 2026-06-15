package br.com.escola.catalogo.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.catalogo.adapter.out.persistence.entity.DisciplinaEntity;
import br.com.escola.catalogo.adapter.out.persistence.entity.PeriodoLetivoEntity;
import br.com.escola.catalogo.adapter.out.persistence.entity.SerieEntity;
import br.com.escola.catalogo.adapter.out.persistence.entity.TurmaDisciplinaEntity;
import br.com.escola.catalogo.adapter.out.persistence.entity.TurmaEntity;
import br.com.escola.catalogo.adapter.out.persistence.entity.TurnoEntity;
import br.com.escola.catalogo.adapter.out.persistence.repository.DisciplinaJpaRepository;
import br.com.escola.catalogo.adapter.out.persistence.repository.PeriodoLetivoJpaRepository;
import br.com.escola.catalogo.adapter.out.persistence.repository.SerieJpaRepository;
import br.com.escola.catalogo.adapter.out.persistence.repository.TurmaDisciplinaJpaRepository;
import br.com.escola.catalogo.adapter.out.persistence.repository.TurmaJpaRepository;
import br.com.escola.catalogo.adapter.out.persistence.repository.TurnoJpaRepository;
import br.com.escola.catalogo.application.dto.internal.DisciplinaResumo;
import br.com.escola.catalogo.application.dto.internal.PeriodoLetivoResumo;
import br.com.escola.catalogo.application.dto.internal.SerieResumo;
import br.com.escola.catalogo.application.dto.internal.TurmaDisciplinaResumo;
import br.com.escola.catalogo.application.dto.internal.TurmaResumo;
import br.com.escola.catalogo.application.dto.internal.TurnoResumo;
import br.com.escola.catalogo.application.port.internal.CatalogoAcademicoPort;
import br.com.escola.catalogo.application.port.internal.EstruturaTurmaPort;
import br.com.escola.institucional.application.port.EscolaContextoPort;

@Service
public class CatalogoAcademicoInternalService implements CatalogoAcademicoPort, EstruturaTurmaPort {

    private final PeriodoLetivoJpaRepository periodoLetivoJpaRepository;
    private final SerieJpaRepository serieJpaRepository;
    private final TurnoJpaRepository turnoJpaRepository;
    private final DisciplinaJpaRepository disciplinaJpaRepository;
    private final TurmaJpaRepository turmaJpaRepository;
    private final TurmaDisciplinaJpaRepository turmaDisciplinaJpaRepository;
    private final EscolaContextoPort escolaContextoPort;

    public CatalogoAcademicoInternalService(
            PeriodoLetivoJpaRepository periodoLetivoJpaRepository,
            SerieJpaRepository serieJpaRepository,
            TurnoJpaRepository turnoJpaRepository,
            DisciplinaJpaRepository disciplinaJpaRepository,
            TurmaJpaRepository turmaJpaRepository,
            TurmaDisciplinaJpaRepository turmaDisciplinaJpaRepository,
            EscolaContextoPort escolaContextoPort) {
        this.periodoLetivoJpaRepository = periodoLetivoJpaRepository;
        this.serieJpaRepository = serieJpaRepository;
        this.turnoJpaRepository = turnoJpaRepository;
        this.disciplinaJpaRepository = disciplinaJpaRepository;
        this.turmaJpaRepository = turmaJpaRepository;
        this.turmaDisciplinaJpaRepository = turmaDisciplinaJpaRepository;
        this.escolaContextoPort = escolaContextoPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PeriodoLetivoResumo> listarPeriodosLetivos(UUID escolaId) {
        return periodoLetivoJpaRepository.findAllByEscola_Id(resolverEscolaId(escolaId)).stream()
                .map(this::toPeriodoLetivoResumo)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PeriodoLetivoResumo> buscarPeriodoLetivo(UUID escolaId, UUID periodoLetivoId) {
        return periodoLetivoJpaRepository.findByIdAndEscola_Id(periodoLetivoId, resolverEscolaId(escolaId))
                .map(this::toPeriodoLetivoResumo);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePeriodoLetivo(UUID escolaId, UUID periodoLetivoId) {
        return periodoLetivoJpaRepository.existsByIdAndEscola_Id(periodoLetivoId, resolverEscolaId(escolaId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SerieResumo> listarSeries(UUID escolaId) {
        return serieJpaRepository.findAllByEscola_Id(resolverEscolaId(escolaId)).stream()
                .map(this::toSerieResumo)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SerieResumo> buscarSerie(UUID escolaId, UUID serieId) {
        return serieJpaRepository.findByIdAndEscola_Id(serieId, resolverEscolaId(escolaId))
                .map(this::toSerieResumo);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeSerie(UUID escolaId, UUID serieId) {
        return serieJpaRepository.existsByIdAndEscola_Id(serieId, resolverEscolaId(escolaId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TurnoResumo> listarTurnos() {
        return turnoJpaRepository.findAll().stream()
                .map(this::toTurnoResumo)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DisciplinaResumo> listarDisciplinas(UUID escolaId) {
        return disciplinaJpaRepository.findAllByEscola_Id(resolverEscolaId(escolaId)).stream()
                .map(this::toDisciplinaResumo)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DisciplinaResumo> buscarDisciplina(UUID escolaId, UUID disciplinaId) {
        return disciplinaJpaRepository.findByIdAndEscola_Id(disciplinaId, resolverEscolaId(escolaId))
                .map(this::toDisciplinaResumo);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeDisciplina(UUID escolaId, UUID disciplinaId) {
        return disciplinaJpaRepository.existsByIdAndEscola_Id(disciplinaId, resolverEscolaId(escolaId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TurmaResumo> listarTurmas(UUID escolaId) {
        return turmaJpaRepository.findAllByEscola_Id(resolverEscolaId(escolaId)).stream()
                .map(this::toTurmaResumo)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TurmaResumo> buscarTurma(UUID escolaId, UUID turmaId) {
        return obterTurma(escolaId, turmaId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeTurma(UUID escolaId, UUID turmaId) {
        return turmaJpaRepository.existsByIdAndEscola_Id(turmaId, resolverEscolaId(escolaId));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TurmaResumo> obterTurma(UUID escolaId, UUID turmaId) {
        return turmaJpaRepository.findByIdAndEscola_Id(turmaId, resolverEscolaId(escolaId))
                .map(this::toTurmaResumo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TurmaDisciplinaResumo> listarDisciplinasDaTurma(UUID escolaId, UUID turmaId) {
        if (!existeTurma(escolaId, turmaId)) {
            return List.of();
        }
        return turmaDisciplinaJpaRepository.findByTurmaId(turmaId).stream()
                .map(this::toTurmaDisciplinaResumo)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean turmaPossuiDisciplina(UUID escolaId, UUID turmaId, UUID disciplinaId) {
        UUID escolaResolvidaId = resolverEscolaId(escolaId);
        if (!turmaJpaRepository.existsByIdAndEscola_Id(turmaId, escolaResolvidaId)) {
            return false;
        }
        return turmaDisciplinaJpaRepository.findByTurmaIdAndDisciplinaId(turmaId, disciplinaId).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean turmaPertenceAoPeriodo(UUID escolaId, UUID turmaId, UUID periodoLetivoId) {
        return turmaJpaRepository.findByIdAndEscola_Id(turmaId, resolverEscolaId(escolaId))
                .map(turma -> turma.getPeriodoLetivo().getId().equals(periodoLetivoId))
                .orElse(false);
    }

    private UUID resolverEscolaId(UUID escolaId) {
        return escolaId == null ? escolaContextoPort.obterContextoPadrao().escolaId() : escolaId;
    }

    private PeriodoLetivoResumo toPeriodoLetivoResumo(PeriodoLetivoEntity entity) {
        return new PeriodoLetivoResumo(
                entity.getId(),
                entity.getNome(),
                entity.getAno(),
                entity.getDataInicio(),
                entity.getDataFim(),
                !Boolean.FALSE.equals(entity.getAtivo()));
    }

    private SerieResumo toSerieResumo(SerieEntity entity) {
        return new SerieResumo(
                entity.getId(),
                entity.getNome(),
                entity.getOrdem(),
                entity.getNivelEnsino());
    }

    private TurnoResumo toTurnoResumo(TurnoEntity entity) {
        return new TurnoResumo(entity.getId(), entity.getCodigo(), entity.getDescricao());
    }

    private DisciplinaResumo toDisciplinaResumo(DisciplinaEntity entity) {
        return new DisciplinaResumo(
                entity.getId(),
                entity.getNome(),
                entity.getCargaHoraria(),
                !"INATIVA".equalsIgnoreCase(entity.getStatus()));
    }

    private TurmaResumo toTurmaResumo(TurmaEntity entity) {
        return new TurmaResumo(
                entity.getId(),
                entity.getCodigo(),
                entity.getNome(),
                entity.getCapacidade(),
                entity.getPeriodoLetivo().getId(),
                entity.getPeriodoLetivo().getNome(),
                entity.getSerie().getId(),
                entity.getSerie().getNome(),
                entity.getEscola().getId(),
                entity.getEscola().getNome(),
                entity.getTurno(),
                !Boolean.FALSE.equals(entity.getAtivo()));
    }

    private TurmaDisciplinaResumo toTurmaDisciplinaResumo(TurmaDisciplinaEntity entity) {
        return new TurmaDisciplinaResumo(
                entity.getId(),
                entity.getTurma().getId(),
                entity.getDisciplina().getId(),
                entity.getDisciplina().getNome(),
                entity.getCargaHoraria());
    }
}
