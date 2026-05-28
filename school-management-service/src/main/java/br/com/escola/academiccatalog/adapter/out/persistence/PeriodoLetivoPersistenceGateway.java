package br.com.escola.academiccatalog.adapter.out.persistence;

import java.util.UUID;

import java.time.LocalDateTime;
import java.util.List;
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
    public Optional<PeriodoLetivoOutput> findById(@NonNull UUID id) {
        return periodoLetivoJpaRepository.findById(id).map(this::toOutput);
    }

    @Override
    public boolean existsById(@NonNull UUID id) {
        return periodoLetivoJpaRepository.existsById(id);
    }

    @Override
    public PeriodoLetivoOutput save(PeriodoLetivoInput input) {
        PeriodoLetivoEntity entity = new PeriodoLetivoEntity();
        entity.setNome(input.nome());
        entity.setAno(resolveAno(input));
        entity.setDataInicio(input.dataInicio());
        entity.setDataFim(input.dataFim());
        entity.setAtivo(true);
        entity.setCreatedAt(LocalDateTime.now());
        return toOutput(periodoLetivoJpaRepository.save(entity));
    }

    @Override
    public List<PeriodoLetivoOutput> findAll() {
        return periodoLetivoJpaRepository.findAll().stream().map(this::toOutput).toList();
    }

    private PeriodoLetivoOutput toOutput(PeriodoLetivoEntity entity) {
        return new PeriodoLetivoOutput(
                entity.getId(),
                entity.getNome(),
                entity.getAno(),
                entity.getDataInicio(),
                entity.getDataFim(),
                entity.getAtivo(),
                entity.getCreatedAt());
    }

    private Integer resolveAno(PeriodoLetivoInput input) {
        if (input.ano() != null) {
            return input.ano();
        }
        return input.dataInicio().getYear();
    }
}
