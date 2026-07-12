package br.com.escola.aluno.application.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.aluno.adapter.out.persistence.entity.AlunoEntity;
import br.com.escola.aluno.adapter.out.persistence.repository.AlunoJpaRepository;
import br.com.escola.aluno.application.port.internal.AlunoMatriculaPort;

@Service
@Primary
public class AlunoMatriculaService implements AlunoMatriculaPort {

    private final AlunoJpaRepository alunoJpaRepository;

    public AlunoMatriculaService(AlunoJpaRepository alunoJpaRepository) {
        this.alunoJpaRepository = alunoJpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeAlunoPorIdEEscola(UUID alunoId, UUID escolaId) {
        if (alunoId == null || escolaId == null) {
            return false;
        }
        return alunoJpaRepository.existsByIdAndPessoa_Escola_Id(alunoId, escolaId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AlunoEntity> buscarAlunoPorIdEEscola(UUID alunoId, UUID escolaId) {
        if (alunoId == null || escolaId == null) {
            return Optional.empty();
        }
        return alunoJpaRepository.findByIdAndPessoa_Escola_Id(alunoId, escolaId);
    }
}
