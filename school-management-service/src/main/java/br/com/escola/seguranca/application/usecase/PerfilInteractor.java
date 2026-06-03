package br.com.escola.seguranca.application.usecase;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.seguranca.application.port.in.PerfilUseCasePort;
import br.com.escola.seguranca.application.port.out.PerfilRepositoryPort;
import br.com.escola.seguranca.domain.model.PerfilModel;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PerfilInteractor implements PerfilUseCasePort {

    private final PerfilRepositoryPort repository;

    @Override
    @Transactional
    public PerfilModel create(PerfilModel domain) {
        Objects.requireNonNull(domain, "O modelo de perfil não pode ser nulo");
        Objects.requireNonNull(domain.getCodigo(), "Código não pode ser nulo");

        if (domain.getCodigo().isBlank()) {
            throw new IllegalArgumentException("Código não pode ser vazio");
        }

        if (repository.existsByCodigo(domain.getCodigo())) {
            throw new IllegalArgumentException("Perfil já existe: " + domain.getCodigo());
        }

        return repository.create(domain);
    }

    @Override
    @Transactional
    public PerfilModel update(UUID id, PerfilModel domain) {
        Objects.requireNonNull(id, "ID não pode ser nulo");
        Objects.requireNonNull(domain, "O modelo de perfil não pode ser nulo");
        Objects.requireNonNull(domain.getCodigo(), "Código não pode ser nulo");

        if (domain.getCodigo().isBlank()) {
            throw new IllegalArgumentException("Código não pode ser vazio");
        }

        repository.findByCodigo(domain.getCodigo())
            .filter(p -> !p.getId().equals(id))
            .ifPresent(p -> {
                throw new IllegalArgumentException(
                    "Já existe outro perfil com este código: " + domain.getCodigo()
                );
            });

        return repository.update(id, domain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PerfilModel> findById(UUID id) {
        Objects.requireNonNull(id, "ID não pode ser nulo");
        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerfilModel> listAll() {
        return repository.listAll();
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Objects.requireNonNull(id, "ID para exclusão não pode ser nulo");
        repository.delete(id);
    }

    @Override
    public boolean existsByCodigo(String codigo) {
        Objects.requireNonNull(codigo, "Código não pode ser nulo");
        return repository.existsByCodigo(codigo);
    }
}