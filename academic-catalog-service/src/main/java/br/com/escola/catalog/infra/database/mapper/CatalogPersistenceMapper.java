package br.com.escola.catalog.infra.database.mapper;

import java.time.LocalDateTime;

import br.com.escola.catalog.domain.model.Disciplina;
import br.com.escola.catalog.domain.model.PeriodoLetivo;
import br.com.escola.catalog.domain.model.Serie;
import br.com.escola.catalog.domain.model.Turma;
import br.com.escola.catalog.domain.model.TurmaDisciplina;
import br.com.escola.catalog.domain.model.Turno;
import br.com.escola.catalog.domain.valueobject.EscolaId;
import br.com.escola.catalog.infra.database.entity.DisciplinaJpaEntity;
import br.com.escola.catalog.infra.database.entity.PeriodoLetivoJpaEntity;
import br.com.escola.catalog.infra.database.entity.SerieJpaEntity;
import br.com.escola.catalog.infra.database.entity.TurmaDisciplinaJpaEntity;
import br.com.escola.catalog.infra.database.entity.TurmaJpaEntity;
import br.com.escola.catalog.infra.database.entity.TurnoJpaEntity;

public final class CatalogPersistenceMapper {

    private CatalogPersistenceMapper() {
    }

    public static PeriodoLetivoJpaEntity toEntity(PeriodoLetivo domain) {
        return new PeriodoLetivoJpaEntity(
                domain.id(), domain.escolaId().value(), domain.nome(), domain.ano(),
                domain.dataInicio(), domain.dataFim(), domain.ativo(), domain.createdAt(), LocalDateTime.now());
    }

    public static PeriodoLetivo toDomain(PeriodoLetivoJpaEntity entity) {
        return new PeriodoLetivo(
                entity.getId(), new EscolaId(entity.getEscolaId()), entity.getNome(), entity.getAno(),
                entity.getDataInicio(), entity.getDataFim(), entity.getAtivo(), entity.getCreatedAt());
    }

    public static SerieJpaEntity toEntity(Serie domain) {
        return new SerieJpaEntity(
                domain.id(), domain.escolaId().value(), domain.nome(), domain.ordem(),
                domain.nivelEnsinoId(), domain.createdAt(), LocalDateTime.now());
    }

    public static Serie toDomain(SerieJpaEntity entity) {
        return new Serie(
                entity.getId(), new EscolaId(entity.getEscolaId()), entity.getNome(), entity.getOrdem(),
                entity.getNivelEnsinoId(), entity.getCreatedAt());
    }

    public static TurnoJpaEntity toEntity(Turno domain) {
        return new TurnoJpaEntity(domain.id(), domain.codigo(), domain.descricao());
    }

    public static Turno toDomain(TurnoJpaEntity entity) {
        return new Turno(entity.getId(), entity.getCodigo(), entity.getDescricao());
    }

    public static DisciplinaJpaEntity toEntity(Disciplina domain) {
        return new DisciplinaJpaEntity(
                domain.id(), domain.escolaId().value(), domain.nome(), domain.cargaHoraria(),
                domain.ativo(), domain.createdAt(), LocalDateTime.now());
    }

    public static Disciplina toDomain(DisciplinaJpaEntity entity) {
        return new Disciplina(
                entity.getId(), new EscolaId(entity.getEscolaId()), entity.getNome(), entity.getCargaHoraria(),
                entity.getAtivo(), entity.getCreatedAt());
    }

    public static TurmaJpaEntity toEntity(Turma domain) {
        return new TurmaJpaEntity(
                domain.id(), domain.escolaId().value(), domain.codigo(), domain.nome(), domain.capacidade(),
                domain.periodoLetivoId(), domain.serieId(), domain.turnoId(), domain.ativo(),
                domain.createdAt(), LocalDateTime.now());
    }

    public static Turma toDomain(TurmaJpaEntity entity) {
        return new Turma(
                entity.getId(), new EscolaId(entity.getEscolaId()), entity.getCodigo(), entity.getNome(),
                entity.getCapacidade(), entity.getPeriodoLetivoId(), entity.getSerieId(), entity.getTurnoId(),
                entity.getAtivo(), entity.getCreatedAt());
    }

    public static TurmaDisciplinaJpaEntity toEntity(TurmaDisciplina domain) {
        return new TurmaDisciplinaJpaEntity(
                domain.id(), domain.escolaId().value(), domain.turmaId(), domain.disciplinaId(),
                domain.cargaHoraria(), domain.createdAt());
    }

    public static TurmaDisciplina toDomain(TurmaDisciplinaJpaEntity entity) {
        return new TurmaDisciplina(
                entity.getId(), new EscolaId(entity.getEscolaId()), entity.getTurmaId(),
                entity.getDisciplinaId(), entity.getCargaHoraria(), entity.getCreatedAt());
    }
}

