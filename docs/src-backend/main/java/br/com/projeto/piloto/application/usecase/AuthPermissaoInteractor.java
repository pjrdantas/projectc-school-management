package br.com.projeto.piloto.application.usecase;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import br.com.projeto.piloto.domain.model.AuthPermissaoModel;
import br.com.projeto.piloto.domain.port.inbound.AuthPermissaoUseCase;
import br.com.projeto.piloto.domain.port.outbound.AuthPermissaoRepositoryPort;

@Service
public class AuthPermissaoInteractor implements AuthPermissaoUseCase {

    private final AuthPermissaoRepositoryPort repository;

    public AuthPermissaoInteractor(AuthPermissaoRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public List<AuthPermissaoModel> listAll() {
        return repository.findAll();
    }

    @Override
    public Optional<AuthPermissaoModel> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public AuthPermissaoModel create(AuthPermissaoModel domain) {
      
        if (domain != null && domain.getNmPermissao() != null &&
                repository.existsByNmPermissao(domain.getNmPermissao())) {
            throw new IllegalArgumentException("Permissão já existe: " + domain.getNmPermissao());
        }
        return repository.save(domain);
    }

    @Override
    public AuthPermissaoModel update(Long id, AuthPermissaoModel domain) {
        Optional<AuthPermissaoModel> existing = repository.findById(id);
        if (!existing.isPresent()) {
            throw new IllegalArgumentException("Permissão não encontrada: " + id);
        }

        AuthPermissaoModel d = existing.get();
        d.setNmPermissao(domain.getNmPermissao());

        return repository.save(d);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsByNmPermissao(String nmPermissao) {
        return repository.existsByNmPermissao(nmPermissao);
    }
}
