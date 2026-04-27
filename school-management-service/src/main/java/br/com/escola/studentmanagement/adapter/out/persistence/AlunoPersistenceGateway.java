package br.com.escola.studentmanagement.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import br.com.escola.studentmanagement.adapter.out.persistence.entity.AlunoEntity;
import br.com.escola.studentmanagement.adapter.out.persistence.repository.AlunoJpaRepository;
import br.com.escola.studentmanagement.application.dto.AlunoInput;
import br.com.escola.studentmanagement.application.dto.AlunoOutput;
import br.com.escola.studentmanagement.application.port.out.AlunoCommandGateway;
import br.com.escola.studentmanagement.application.port.out.AlunoQueryGateway;

@Component
public class AlunoPersistenceGateway implements AlunoCommandGateway, AlunoQueryGateway {

    private final AlunoJpaRepository alunoJpaRepository;

    public AlunoPersistenceGateway(AlunoJpaRepository alunoJpaRepository) {
        this.alunoJpaRepository = alunoJpaRepository;
    }

    @Override
    public boolean existsByCpf(String cpf) {
        return alunoJpaRepository.findByCpf(cpf).isPresent();
    }

    @Override
    public AlunoOutput save(AlunoInput input) {
        AlunoEntity alunoEntity = new AlunoEntity();
        alunoEntity.setNomeCompleto(input.nomeCompleto());
        alunoEntity.setCpf(input.cpf());
        alunoEntity.setEmail(input.email());
        alunoEntity.setDataNascimento(input.dataNascimento());
        alunoEntity.setCreatedAt(LocalDateTime.now());
        return toOutput(alunoJpaRepository.save(alunoEntity));
    }

    @Override
    public Optional<AlunoOutput> findById(@NonNull Long id) {
        return alunoJpaRepository.findById(id).map(this::toOutput);
    }

    @Override
    public boolean existsById(@NonNull Long id) {
        return alunoJpaRepository.existsById(id);
    }

    private AlunoOutput toOutput(AlunoEntity entity) {
        return new AlunoOutput(
                entity.getId(),
                entity.getNomeCompleto(),
                entity.getCpf(),
                entity.getEmail(),
                entity.getDataNascimento(),
                entity.getCreatedAt());
    }
}
