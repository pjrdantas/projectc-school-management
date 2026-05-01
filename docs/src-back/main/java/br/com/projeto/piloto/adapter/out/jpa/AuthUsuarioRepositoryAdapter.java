package br.com.projeto.piloto.adapter.out.jpa;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;

import br.com.projeto.piloto.adapter.out.jpa.entity.AuthUsuario;
import br.com.projeto.piloto.adapter.out.jpa.repository.SpringAuthUsuarioRepository;
import br.com.projeto.piloto.domain.port.outbound.AuthUsuarioRepositoryPort;

@Component
public class AuthUsuarioRepositoryAdapter implements AuthUsuarioRepositoryPort {

    private final SpringAuthUsuarioRepository repository;

    public AuthUsuarioRepositoryAdapter(SpringAuthUsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    public AuthUsuario save(AuthUsuario usuario) {
        Objects.requireNonNull(usuario, "usuario não pode ser nulo");
        return repository.save(usuario);
    }

    @Override
    public Optional<AuthUsuario> findById(Long id) {
        Objects.requireNonNull(id, "id não pode ser nulo");
        return repository.findById(id);
    }

    @Override
    public Optional<AuthUsuario> findByLogin(String login) {
        return repository.findByLogin(login);
    }

    @Override
    public List<AuthUsuario> findAll() {
        return repository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        Objects.requireNonNull(id, "id não pode ser nulo");
        repository.deleteById(id); 
    }
    
    @Override
    public boolean existsByDsLoginAndIdNot(String login, Long id) {
        return repository.existsByLoginAndIdNot(login, id);
    }
    
    @Override
    public boolean existsById(Long id) {
        Objects.requireNonNull(id, "id não pode ser nulo");
        return repository.existsById(id);
    }
}
