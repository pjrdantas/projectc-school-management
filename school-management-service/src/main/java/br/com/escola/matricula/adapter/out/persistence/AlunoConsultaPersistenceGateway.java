package br.com.escola.matricula.adapter.out.persistence;

import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import br.com.escola.matricula.application.port.out.AlunoConsultaGateway;
import br.com.escola.aluno.adapter.out.persistence.repository.AlunoJpaRepository;
import br.com.escola.institucional.application.service.EscolaTenantService;

@Component
public class AlunoConsultaPersistenceGateway implements AlunoConsultaGateway {

    private final AlunoJpaRepository alunoJpaRepository;
    private final EscolaTenantService escolaTenantService;

    public AlunoConsultaPersistenceGateway(
            AlunoJpaRepository alunoJpaRepository,
            EscolaTenantService escolaTenantService) {
        this.alunoJpaRepository = alunoJpaRepository;
        this.escolaTenantService = escolaTenantService;
    }

    @Override
    public boolean existsById(@NonNull UUID id) {
        return alunoJpaRepository.existsByIdAndPessoa_Escola_Id(id, escolaTenantService.obterOuCriarEscolaPadrao().getId());
    }
}
