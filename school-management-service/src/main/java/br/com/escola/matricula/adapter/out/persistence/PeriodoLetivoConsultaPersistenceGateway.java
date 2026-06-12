package br.com.escola.matricula.adapter.out.persistence;

import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import br.com.escola.catalogo.adapter.out.persistence.repository.PeriodoLetivoJpaRepository;
import br.com.escola.institucional.application.service.EscolaTenantService;
import br.com.escola.matricula.application.port.out.PeriodoLetivoConsultaGateway;

@Component
public class PeriodoLetivoConsultaPersistenceGateway implements PeriodoLetivoConsultaGateway {

    private final PeriodoLetivoJpaRepository periodoLetivoJpaRepository;
    private final EscolaTenantService escolaTenantService;

    public PeriodoLetivoConsultaPersistenceGateway(
            PeriodoLetivoJpaRepository periodoLetivoJpaRepository,
            EscolaTenantService escolaTenantService) {
        this.periodoLetivoJpaRepository = periodoLetivoJpaRepository;
        this.escolaTenantService = escolaTenantService;
    }

    @Override
    public boolean existsById(@NonNull UUID id) {
        return periodoLetivoJpaRepository.existsByIdAndEscola_Id(id, escolaTenantService.obterOuCriarEscolaPadrao().getId());
    }
}
