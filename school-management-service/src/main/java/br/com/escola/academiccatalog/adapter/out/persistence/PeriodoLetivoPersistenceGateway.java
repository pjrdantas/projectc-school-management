package br.com.escola.academiccatalog.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import br.com.escola.academiccatalog.adapter.out.persistence.entity.PeriodoLetivoEntity;
import br.com.escola.academiccatalog.adapter.out.persistence.repository.PeriodoLetivoJpaRepository;
import br.com.escola.academiccatalog.application.dto.PeriodoLetivoInput;
import br.com.escola.academiccatalog.application.dto.PeriodoLetivoOutput;
import br.com.escola.academiccatalog.application.port.out.PeriodoLetivoGateway;

@Component
public class PeriodoLetivoPersistenceGateway implements PeriodoLetivoGateway {

    private final PeriodoLetivoJpaRepository periodoLetivoJpaRepository;

    public PeriodoLetivoPersistenceGateway(PeriodoLetivoJpaRepository periodoLetivoJpaRepository) {
        this.periodoLetivoJpaRepository = periodoLetivoJpaRepository;
    }

    @Override
    public Optional<PeriodoLetivoOutput> findById(@NonNull Long id) {
        return periodoLetivoJpaRepository.findById(id).map(this::toOutput);
    }

    @Override
    public boolean existsById(@NonNull Long id) {
        return periodoLetivoJpaRepository.existsById(id);
    }

    @Override
    public PeriodoLetivoOutput save(PeriodoLetivoInput input) {
        PeriodoLetivoEntity entity = new PeriodoLetivoEntity();
        entity.setNome(input.nome());
        entity.setDataInicio(input.dataInicio());
        entity.setDataFim(input.dataFim());
        entity.setCreatedAt(LocalDateTime.now());
        return toOutput(periodoLetivoJpaRepository.save(entity));
    }

    private PeriodoLetivoOutput toOutput(PeriodoLetivoEntity entity) {
        return new PeriodoLetivoOutput(
                entity.getId(),
                entity.getNome(),
                entity.getDataInicio(),
                entity.getDataFim(),
                entity.getCreatedAt());
    }
}
