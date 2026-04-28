package br.com.escola.enrollment.adapter.out.persistence;

import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import br.com.escola.enrollment.application.port.out.AlunoConsultaGateway;
import br.com.escola.studentmanagement.adapter.out.persistence.repository.AlunoJpaRepository;

@Component
public class AlunoConsultaPersistenceGateway implements AlunoConsultaGateway {

    private final AlunoJpaRepository alunoJpaRepository;

    public AlunoConsultaPersistenceGateway(AlunoJpaRepository alunoJpaRepository) {
        this.alunoJpaRepository = alunoJpaRepository;
    }

    @Override
    public boolean existsById(@NonNull UUID id) {
        return alunoJpaRepository.existsById(id);
    }
}
