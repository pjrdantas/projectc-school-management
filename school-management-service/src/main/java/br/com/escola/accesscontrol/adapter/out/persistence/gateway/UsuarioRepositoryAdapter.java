package br.com.escola.accesscontrol.adapter.out.persistence.gateway;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import br.com.escola.accesscontrol.adapter.out.persistence.entity.UsuarioEntity;
import br.com.escola.accesscontrol.adapter.out.persistence.repository.SpringUsuarioJpaRepository;
import br.com.escola.accesscontrol.application.port.out.UsuarioRepositoryPort;

@Component
public class UsuarioRepositoryAdapter implements UsuarioRepositoryPort {

    private final SpringUsuarioJpaRepository repository;

    public UsuarioRepositoryAdapter(SpringUsuarioJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public UsuarioEntity save(UsuarioEntity usuario) {
        Objects.requireNonNull(usuario, "usuario não pode ser nulo");
        return repository.save(usuario);
    }

    @Override
    public Optional<UsuarioEntity> findById(UUID id) {
        Objects.requireNonNull(id, "id não pode ser nulo");
        return repository.findById(id);
    }

    @Override
    public Optional<UsuarioEntity> findByUsername(String username) {
        Objects.requireNonNull(username, "username não pode ser nulo");
        return repository.findByUsernameIgnoreCase(username);
    }

    @Override
    public List<UsuarioEntity> findAll() {
        return repository.findAll();
    }

    @Override
    public void deleteById(UUID id) {
        Objects.requireNonNull(id, "id não pode ser nulo");
        repository.deleteById(id);
    }

    @Override
    public boolean existsByUsernameAndIdNot(String username, UUID id) {
        Objects.requireNonNull(username, "username não pode ser nulo");
        Objects.requireNonNull(id, "id não pode ser nulo");
        return repository.existsByUsernameAndIdNot(username, id);
    }


    @Override
    public boolean existsByEmailAndIdNot(String email, UUID id) {
        Objects.requireNonNull(email, "email não pode ser nulo");
        Objects.requireNonNull(id, "id não pode ser nulo");
        return repository.existsByEmailAndIdNot(email, id);
    }

    @Override
    public boolean existsById(UUID id) {
        Objects.requireNonNull(id, "id não pode ser nulo");
        return repository.existsById(id);
    }

    @Override
    public boolean existsByUsername(String username) {
        Objects.requireNonNull(username, "username não pode ser nulo");
        return repository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        Objects.requireNonNull(email, "email não pode ser nulo");
        return repository.existsByEmail(email);
    }
}