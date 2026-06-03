package br.com.escola.catalogo.adapter.out.persistence;

import java.util.UUID;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.catalogo.adapter.out.persistence.entity.PeriodoLetivoEntity;
import br.com.escola.catalogo.adapter.out.persistence.entity.SerieEntity;
import br.com.escola.catalogo.adapter.out.persistence.entity.TurmaEntity;
import br.com.escola.catalogo.adapter.out.persistence.repository.PeriodoLetivoJpaRepository;
import br.com.escola.catalogo.adapter.out.persistence.repository.SerieJpaRepository;
import br.com.escola.catalogo.adapter.out.persistence.repository.TurmaJpaRepository;
import br.com.escola.catalogo.adapter.out.persistence.repository.TurnoJpaRepository;
import br.com.escola.catalogo.application.dto.TurmaInput;
import br.com.escola.catalogo.application.dto.TurmaOutput;
import br.com.escola.catalogo.application.port.out.TurmaGateway;
import br.com.escola.catalogo.domain.exception.SerieNaoEncontradaException;
import br.com.escola.catalogo.domain.exception.TurmaNaoEncontradaException;

@Component
public class TurmaPersistenceGateway implements TurmaGateway {

    private final TurmaJpaRepository turmaJpaRepository;
    private final PeriodoLetivoJpaRepository periodoLetivoJpaRepository;
    private final SerieJpaRepository serieJpaRepository;
    private final TurnoJpaRepository turnoJpaRepository;

    public TurmaPersistenceGateway(
            TurmaJpaRepository turmaJpaRepository,
            PeriodoLetivoJpaRepository periodoLetivoJpaRepository,
            SerieJpaRepository serieJpaRepository,
            TurnoJpaRepository turnoJpaRepository) {
        this.turmaJpaRepository = turmaJpaRepository;
        this.periodoLetivoJpaRepository = periodoLetivoJpaRepository;
        this.serieJpaRepository = serieJpaRepository;
        this.turnoJpaRepository = turnoJpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TurmaOutput> findById(@NonNull UUID id) {
        return turmaJpaRepository.findById(id).map(this::toOutput);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TurmaOutput> findByCodigoAndPeriodoLetivoId(String codigo, UUID periodoLetivoId) {
        return turmaJpaRepository.findByCodigoAndPeriodoLetivo_Id(codigo, periodoLetivoId).map(this::toOutput);
    }

    @Override
    @Transactional
    public TurmaOutput save(TurmaInput input) {
        PeriodoLetivoEntity periodo = periodoLetivoJpaRepository.getReferenceById(input.periodoLetivoId());
        SerieEntity serie = serieJpaRepository.findById(input.serieId())
                .orElseThrow(() -> new SerieNaoEncontradaException(input.serieId()));
        TurmaEntity entity = new TurmaEntity();
        entity.setCodigo(input.codigo());
        entity.setNome(input.nome());
        entity.setCapacidade(input.capacidade());
        entity.setPeriodoLetivo(periodo);
        entity.setSerie(serie);
        entity.setTurno(resolveTurno(input.turno()));
        entity.setAtivo(resolveAtivo(input.status()));
        entity.setCreatedAt(LocalDateTime.now());
        return toOutput(turmaJpaRepository.save(entity));
    }

    @Override
    @Transactional
    public TurmaOutput update(UUID id, TurmaInput input) {
        TurmaEntity entity = turmaJpaRepository.findById(id)
                .orElseThrow(() -> new TurmaNaoEncontradaException(id));
        PeriodoLetivoEntity periodo = periodoLetivoJpaRepository.getReferenceById(input.periodoLetivoId());
        SerieEntity serie = serieJpaRepository.findById(input.serieId())
                .orElseThrow(() -> new SerieNaoEncontradaException(input.serieId()));

        entity.setCodigo(input.codigo());
        entity.setNome(input.nome());
        entity.setCapacidade(input.capacidade());
        entity.setPeriodoLetivo(periodo);
        entity.setSerie(serie);
        entity.setTurno(resolveTurno(input.turno()));
        entity.setAtivo(resolveAtivo(input.status()));

        return toOutput(turmaJpaRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TurmaOutput> findAll() {
        return turmaJpaRepository.findAll().stream().map(this::toOutput).toList();
    }

    private TurmaOutput toOutput(TurmaEntity entity) {
        return new TurmaOutput(
                entity.getId(),
                entity.getCodigo(),
                entity.getNome(),
                entity.getCapacidade(),
                entity.getPeriodoLetivo().getId(),
                entity.getSerie().getId(),
                entity.getSerie().getNome(),
                entity.getTurno(),
                entity.getStatus(),
                entity.getCreatedAt());
    }

    private br.com.escola.catalogo.adapter.out.persistence.entity.TurnoEntity resolveTurno(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return null;
        }
        return turnoJpaRepository.findByCodigoIgnoreCase(codigo.trim())
                .orElseThrow(() -> new IllegalArgumentException("Turno não encontrado: " + codigo));
    }

    private Boolean resolveAtivo(String status) {
        if (status == null || status.isBlank()) {
            return true;
        }
        String normalized = status.trim();
        return !normalized.equalsIgnoreCase("INATIVA")
                && !normalized.equalsIgnoreCase("INATIVO")
                && !normalized.equalsIgnoreCase("false");
    }
}
