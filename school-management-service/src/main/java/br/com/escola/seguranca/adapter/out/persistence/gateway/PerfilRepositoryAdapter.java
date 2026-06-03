package br.com.escola.seguranca.adapter.out.persistence.gateway;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.seguranca.adapter.out.persistence.mapper.PerfilMapper;
import br.com.escola.seguranca.adapter.out.persistence.repository.SpringPerfilJpaRepository;
import br.com.escola.seguranca.application.port.out.PerfilRepositoryPort;
import br.com.escola.seguranca.domain.model.PerfilModel;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PerfilRepositoryAdapter implements PerfilRepositoryPort {

    private final SpringPerfilJpaRepository repository;

    @Override
    public @NonNull List<PerfilModel> listAll() {
        List<PerfilModel> list = repository.findAll()
                .stream()
                .map(PerfilMapper::toDomain)
                .collect(Collectors.toList());

        return Objects.requireNonNull(list);
    }

    @Override
    public Optional<PerfilModel> findById(UUID id) {
        Objects.requireNonNull(id, "ID não pode ser nulo");

        return repository.findById(id)
                .map(PerfilMapper::toDomain);
    }

    @Override
    public Optional<PerfilModel> findByCodigo(String codigo) {
        Objects.requireNonNull(codigo, "Código não pode ser nulo");

        return repository.findByCodigo(codigo)
                .map(PerfilMapper::toDomain);
    }

    @Override
    @Transactional
    public @NonNull PerfilModel create(@NonNull PerfilModel domain) {
        UUID perfilId = UUID.randomUUID();
        repository.insertPerfil(perfilId, domain.getCodigo(), domain.getNome(), domain.getDescricao());

        if (domain.getPermissoes() != null) {
            domain.getPermissoes().forEach(permissao ->
                    repository.insertPerfilPermissao(UUID.randomUUID(), perfilId, permissao.getId()));
        }

        return repository.findById(perfilId)
                .map(PerfilMapper::toDomain)
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado após criação: " + perfilId));
    }

    @Override
    @Transactional
    public @NonNull PerfilModel update(@NonNull UUID id, @NonNull PerfilModel domain) {
        Objects.requireNonNull(id, "ID não pode ser nulo");
        Objects.requireNonNull(domain, "PerfilModel não pode ser nulo");

        PerfilModel updated = repository.findById(id)
                .map(existing -> {

                    existing.setCodigo(domain.getCodigo());
                    existing.setNome(domain.getNome());
                    existing.setDescricao(domain.getDescricao());

                    repository.updatePerfilFields(existing.getId(), domain.getCodigo(), domain.getNome(), domain.getDescricao());

                    repository.deletePermissoesByPerfilId(existing.getId());

                    if (domain.getPermissoes() != null) {
                        domain.getPermissoes().forEach(permissao ->
                                repository.insertPerfilPermissao(UUID.randomUUID(), existing.getId(), permissao.getId()));
                    }

                    return repository.findById(existing.getId())
                            .map(PerfilMapper::toDomain)
                            .orElseThrow(() -> new RuntimeException("Perfil não encontrado após atualização: " + existing.getId()));
                })
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado: " + id));

        return Objects.requireNonNull(updated);
    }

    @Override
    public void delete(@NonNull UUID id) {
        Objects.requireNonNull(id, "ID não pode ser nulo");
        repository.deleteById(id);
    }

    @Override
    public boolean existsByCodigo(String codigo) {
        return repository.existsByCodigo(codigo);
    }
}