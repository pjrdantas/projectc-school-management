package br.com.escola.matricula.adapter.out.persistence;

import java.util.UUID;

import java.util.Optional;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.catalogo.adapter.out.persistence.repository.TurmaJpaRepository;
import br.com.escola.institucional.application.service.EscolaTenantService;
import br.com.escola.matricula.application.port.out.TurmaConsultaGateway;

@Component
public class TurmaConsultaPersistenceGateway implements TurmaConsultaGateway {

    private final TurmaJpaRepository turmaJpaRepository;
    private final EscolaTenantService escolaTenantService;

    public TurmaConsultaPersistenceGateway(
            TurmaJpaRepository turmaJpaRepository,
            EscolaTenantService escolaTenantService) {
        this.turmaJpaRepository = turmaJpaRepository;
        this.escolaTenantService = escolaTenantService;
    }

    @Override
    public boolean existsById(@NonNull UUID id) {
        return turmaJpaRepository.existsByIdAndEscola_Id(id, escolaId());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UUID> findPeriodoLetivoIdByTurmaId(@NonNull UUID turmaId) {
        return turmaJpaRepository.findByIdAndEscola_Id(turmaId, escolaId()).map(turma -> turma.getPeriodoLetivo().getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Integer> findCapacidadeByTurmaId(@NonNull UUID turmaId) {
        return turmaJpaRepository.findByIdAndEscola_Id(turmaId, escolaId()).map(turma -> turma.getCapacidade());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UUID> findSerieIdByTurmaId(@NonNull UUID turmaId) {
        return turmaJpaRepository.findByIdAndEscola_Id(turmaId, escolaId()).map(turma -> turma.getSerie().getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Integer> findSerieOrdemByTurmaId(@NonNull UUID turmaId) {
        return turmaJpaRepository.findByIdAndEscola_Id(turmaId, escolaId()).map(turma -> turma.getSerie().getOrdem());
    }

    private UUID escolaId() {
        return escolaTenantService.obterOuCriarEscolaPadrao().getId();
    }
}
