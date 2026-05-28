package br.com.escola.enrollment.adapter.out.persistence;

import java.util.UUID;

import java.util.Optional;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.academiccatalog.adapter.out.persistence.repository.TurmaJpaRepository;
import br.com.escola.enrollment.application.port.out.TurmaConsultaGateway;

@Component
public class TurmaConsultaPersistenceGateway implements TurmaConsultaGateway {

    private final TurmaJpaRepository turmaJpaRepository;

    public TurmaConsultaPersistenceGateway(TurmaJpaRepository turmaJpaRepository) {
        this.turmaJpaRepository = turmaJpaRepository;
    }

    @Override
    public boolean existsById(@NonNull UUID id) {
        return turmaJpaRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UUID> findPeriodoLetivoIdByTurmaId(@NonNull UUID turmaId) {
        return turmaJpaRepository.findById(turmaId).map(turma -> turma.getPeriodoLetivo().getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Integer> findCapacidadeByTurmaId(@NonNull UUID turmaId) {
        return turmaJpaRepository.findById(turmaId).map(turma -> turma.getCapacidade());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UUID> findSerieIdByTurmaId(@NonNull UUID turmaId) {
        return turmaJpaRepository.findById(turmaId).map(turma -> turma.getSerie().getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Integer> findSerieOrdemByTurmaId(@NonNull UUID turmaId) {
        return turmaJpaRepository.findById(turmaId).map(turma -> turma.getSerie().getOrdem());
    }
}
