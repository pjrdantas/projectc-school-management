package br.com.escola.responsavelmanagement.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import br.com.escola.responsavelmanagement.adapter.out.persistence.entity.ResponsavelEntity;
import br.com.escola.responsavelmanagement.adapter.out.persistence.repository.ResponsavelJpaRepository;
import br.com.escola.responsavelmanagement.application.dto.ResponsavelInput;
import br.com.escola.responsavelmanagement.application.dto.ResponsavelOutput;
import br.com.escola.responsavelmanagement.application.port.out.ResponsavelCommandGateway;
import br.com.escola.responsavelmanagement.application.port.out.ResponsavelQueryGateway;
import br.com.escola.responsavelmanagement.domain.exception.ResponsavelNaoEncontradoException;

@Component
public class ResponsavelPersistenceGateway implements ResponsavelCommandGateway, ResponsavelQueryGateway {

    private final ResponsavelJpaRepository responsavelJpaRepository;

    public ResponsavelPersistenceGateway(ResponsavelJpaRepository responsavelJpaRepository) {
        this.responsavelJpaRepository = responsavelJpaRepository;
    }

    @Override
    public boolean existsByCpf(String cpf) {
        return responsavelJpaRepository.findByCpf(cpf).isPresent();
    }

    @Override
    public boolean existsByCpfAndIdNot(String cpf, UUID id) {
        return responsavelJpaRepository.existsByCpfAndIdNot(cpf, id);
    }

    @Override
    public ResponsavelOutput save(ResponsavelInput input) {
        ResponsavelEntity entity = new ResponsavelEntity();
        entity.setNomeCompleto(input.nomeCompleto());
        entity.setCpf(input.cpf());
        entity.setEmail(input.email());
        entity.setTelefone(input.telefone());
        return toOutput(responsavelJpaRepository.save(entity));
    }

    @Override
    public ResponsavelOutput update(@NonNull UUID id, ResponsavelInput input) {
        ResponsavelEntity entity = responsavelJpaRepository.findById(id)
                .orElseThrow(() -> new ResponsavelNaoEncontradoException(id));

        entity.setNomeCompleto(input.nomeCompleto());
        entity.setCpf(input.cpf());
        entity.setEmail(input.email());
        entity.setTelefone(input.telefone());
        return toOutput(responsavelJpaRepository.save(entity));
    }

    @Override
    public void deleteById(@NonNull UUID id) {
        responsavelJpaRepository.deleteById(id);
    }

    @Override
    public Optional<ResponsavelOutput> findById(@NonNull UUID id) {
        return responsavelJpaRepository.findById(id).map(this::toOutput);
    }

    @Override
    public List<ResponsavelOutput> findAll() {
        return responsavelJpaRepository.findAll().stream().map(this::toOutput).toList();
    }

    @Override
    public boolean existsById(@NonNull UUID id) {
        return responsavelJpaRepository.existsById(id);
    }

    private ResponsavelOutput toOutput(ResponsavelEntity entity) {
        return new ResponsavelOutput(
                entity.getId(),
                entity.getNomeCompleto(),
                entity.getCpf(),
                entity.getEmail(),
                entity.getTelefone(),
                entity.getCreatedAt());
    }
}
