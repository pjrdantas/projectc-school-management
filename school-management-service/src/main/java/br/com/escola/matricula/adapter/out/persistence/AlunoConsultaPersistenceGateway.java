package br.com.escola.matricula.adapter.out.persistence;

import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import br.com.escola.matricula.application.port.out.AlunoConsultaGateway;
import br.com.escola.aluno.adapter.out.persistence.repository.AlunoJpaRepository;
import br.com.escola.institucional.application.port.EscolaContextoPort;

@Component
public class AlunoConsultaPersistenceGateway implements AlunoConsultaGateway {

    private final AlunoJpaRepository alunoJpaRepository;
    private final EscolaContextoPort escolaContextoPort;

    public AlunoConsultaPersistenceGateway(
            AlunoJpaRepository alunoJpaRepository,
            EscolaContextoPort escolaContextoPort) {
        this.alunoJpaRepository = alunoJpaRepository;
        this.escolaContextoPort = escolaContextoPort;
    }

    @Override
    public boolean existsById(@NonNull UUID id) {
        return alunoJpaRepository.existsByIdAndPessoa_Escola_Id(id, escolaContextoPort.obterContextoPadrao().escolaId());
    }
}
