package br.com.escola.accesscontrol.application.usecase;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.accesscontrol.application.port.in.PermissaoUseCasePort;
import br.com.escola.accesscontrol.application.port.out.PermissaoRepositoryPort;
import br.com.escola.accesscontrol.domain.model.PermissaoModel;

@Service
public class PermissaoInteractor implements PermissaoUseCasePort {

    private final PermissaoRepositoryPort repository;

    public PermissaoInteractor(PermissaoRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public List<PermissaoModel> listAll() {
        return repository.findAll();
    }

    @Override
    public Optional<PermissaoModel> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public PermissaoModel create(PermissaoModel domain) {

        if (domain != null && domain.getCodigo() != null &&
                repository.existsByCodigo(domain.getCodigo())) {
            throw new IllegalArgumentException("Permissão já existe: " + domain.getCodigo());
        }

        return repository.save(domain);
    }

    @Override
    public PermissaoModel update(UUID id, PermissaoModel domain) {

        PermissaoModel existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Permissão não encontrada: " + id));

        // 🔥 valida duplicidade (melhoria)
        if (domain.getCodigo() != null &&
                !domain.getCodigo().equalsIgnoreCase(existing.getCodigo()) &&
                repository.existsByCodigo(domain.getCodigo())) {
            throw new IllegalArgumentException("Permissão já existe: " + domain.getCodigo());
        }

        existing.setCodigo(domain.getCodigo());
        existing.setDescricao(domain.getDescricao());

        return repository.save(existing);
    }

    @Override
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsByCodigo(String codigo) {
        return repository.existsByCodigo(codigo);
    }
}