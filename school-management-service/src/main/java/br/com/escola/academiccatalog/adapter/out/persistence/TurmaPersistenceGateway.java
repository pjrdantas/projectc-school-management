package br.com.escola.academiccatalog.adapter.out.persistence;

import java.util.UUID;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import br.com.escola.academiccatalog.adapter.out.persistence.entity.PeriodoLetivoEntity;
import br.com.escola.academiccatalog.adapter.out.persistence.entity.TurmaEntity;
import br.com.escola.academiccatalog.adapter.out.persistence.repository.PeriodoLetivoJpaRepository;
import br.com.escola.academiccatalog.adapter.out.persistence.repository.TurmaJpaRepository;
import br.com.escola.academiccatalog.application.dto.TurmaInput;
import br.com.escola.academiccatalog.application.dto.TurmaOutput;
import br.com.escola.academiccatalog.application.port.out.TurmaGateway;

@Component
public class TurmaPersistenceGateway implements TurmaGateway {

    private final TurmaJpaRepository turmaJpaRepository;
    private final PeriodoLetivoJpaRepository periodoLetivoJpaRepository;

    public TurmaPersistenceGateway(
            TurmaJpaRepository turmaJpaRepository,
            PeriodoLetivoJpaRepository periodoLetivoJpaRepository) {
        this.turmaJpaRepository = turmaJpaRepository;
        this.periodoLetivoJpaRepository = periodoLetivoJpaRepository;
    }

    @Override
    public Optional<TurmaOutput> findById(@NonNull UUID id) {
        return turmaJpaRepository.findById(id).map(this::toOutput);
    }

    @Override
    public Optional<TurmaOutput> findByCodigoAndPeriodoLetivoId(String codigo, UUID periodoLetivoId) {
        return turmaJpaRepository.findByCodigoAndPeriodoLetivoId(codigo, periodoLetivoId).map(this::toOutput);
    }

    @Override
    public TurmaOutput save(TurmaInput input) {
        PeriodoLetivoEntity periodo = periodoLetivoJpaRepository.getReferenceById(input.periodoLetivoId());
        TurmaEntity entity = new TurmaEntity();
        entity.setCodigo(input.codigo());
        entity.setNome(input.nome());
        entity.setCapacidade(input.capacidade());
        entity.setPeriodoLetivo(periodo);
        entity.setCreatedAt(LocalDateTime.now());
        return toOutput(turmaJpaRepository.save(entity));
    }

    @Override
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
                entity.getCreatedAt());
    }
}
