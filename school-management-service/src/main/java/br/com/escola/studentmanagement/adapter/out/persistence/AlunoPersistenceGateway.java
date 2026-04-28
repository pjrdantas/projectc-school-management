package br.com.escola.studentmanagement.adapter.out.persistence;

import java.util.UUID;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import br.com.escola.studentmanagement.adapter.out.persistence.entity.AlunoEntity;
import br.com.escola.studentmanagement.adapter.out.persistence.repository.AlunoJpaRepository;
import br.com.escola.studentmanagement.application.dto.AlunoInput;
import br.com.escola.studentmanagement.application.dto.AlunoOutput;
import br.com.escola.studentmanagement.application.port.out.AlunoCommandGateway;
import br.com.escola.studentmanagement.application.port.out.AlunoQueryGateway;
import br.com.escola.studentmanagement.domain.exception.AlunoNaoEncontradoException;

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
    public boolean existsByCpfAndIdNot(String cpf, @NonNull UUID id) {
        return alunoJpaRepository.existsByCpfAndIdNot(cpf, id);
    }

    @Override
    public AlunoOutput save(AlunoInput input) {
        AlunoEntity alunoEntity = new AlunoEntity();
        alunoEntity.setNomeCompleto(input.nomeCompleto());
        alunoEntity.setCpf(input.cpf());
        alunoEntity.setEmail(input.email());
        alunoEntity.setTelefone(input.telefone());
        alunoEntity.setDataNascimento(input.dataNascimento());
        alunoEntity.setCreatedAt(LocalDateTime.now());
        return toOutput(alunoJpaRepository.save(alunoEntity));
    }

    @Override
    public AlunoOutput update(@NonNull UUID id, AlunoInput input) {
        AlunoEntity alunoEntity = alunoJpaRepository.findById(id)
                .orElseThrow(() -> new AlunoNaoEncontradoException(id));

        alunoEntity.setNomeCompleto(input.nomeCompleto());
        alunoEntity.setCpf(input.cpf());
        alunoEntity.setEmail(input.email());
        alunoEntity.setTelefone(input.telefone());
        alunoEntity.setDataNascimento(input.dataNascimento());
        return toOutput(alunoJpaRepository.save(alunoEntity));
    }

    @Override
    public void deleteById(@NonNull UUID id) {
        alunoJpaRepository.deleteById(id);
    }

    @Override
    public Optional<AlunoOutput> findById(@NonNull UUID id) {
        return alunoJpaRepository.findById(id).map(this::toOutput);
    }

    @Override
    public List<AlunoOutput> findAll() {
        return alunoJpaRepository.findAll().stream().map(this::toOutput).toList();
    }

    @Override
    public boolean existsById(@NonNull UUID id) {
        return alunoJpaRepository.existsById(id);
    }

    private AlunoOutput toOutput(AlunoEntity entity) {
        return new AlunoOutput(
                entity.getId(),
                entity.getNomeCompleto(),
                entity.getCpf(),
                entity.getEmail(),
                entity.getTelefone(),
                entity.getDataNascimento(),
                entity.getCreatedAt());
    }
}
