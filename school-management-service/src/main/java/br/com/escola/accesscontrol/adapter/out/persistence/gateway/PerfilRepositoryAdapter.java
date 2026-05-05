package br.com.escola.accesscontrol.adapter.out.persistence.gateway;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.accesscontrol.adapter.out.persistence.entity.PerfilEntity;
import br.com.escola.accesscontrol.adapter.out.persistence.mapper.PerfilMapper;
import br.com.escola.accesscontrol.adapter.out.persistence.mapper.PermissaoMapper;
import br.com.escola.accesscontrol.adapter.out.persistence.repository.SpringPerfilJpaRepository;
import br.com.escola.accesscontrol.application.port.out.PerfilRepositoryPort;
import br.com.escola.accesscontrol.domain.model.PerfilModel;
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
        PerfilEntity entity = PerfilMapper.toEntity(domain);
        PerfilEntity saved = repository.save(entity);

        return Objects.requireNonNull(PerfilMapper.toDomain(saved));
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

                    existing.getPermissoes().clear();

                    if (domain.getPermissoes() != null) {
                        existing.getPermissoes().addAll(
                                domain.getPermissoes()
                                        .stream()
                                        .map(PermissaoMapper::toEntity)
                                        .collect(Collectors.toSet())
                        );
                    }

                    PerfilEntity saved = repository.save(existing);
                    return PerfilMapper.toDomain(saved);
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