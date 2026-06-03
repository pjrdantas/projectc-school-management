package br.com.escola.seguranca.adapter.out.persistence.gateway;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.seguranca.adapter.out.persistence.entity.PermissaoEntity;
import br.com.escola.seguranca.adapter.out.persistence.repository.SpringPermissaoJpaRepository;
import br.com.escola.seguranca.application.port.out.PermissaoRepositoryPort;
import br.com.escola.seguranca.domain.model.PermissaoModel;
import lombok.RequiredArgsConstructor;

@Component
@Transactional
@RequiredArgsConstructor
public class PermissaoRepositoryAdapter implements PermissaoRepositoryPort {

    private final SpringPermissaoJpaRepository repository;

    // ===== CONVERSÕES =====

    private PermissaoEntity toEntity(PermissaoModel model) {
        if (model == null) return null;

        return PermissaoEntity.builder()
                .id(model.getId())
                .codigo(model.getCodigo())
                .descricao(model.getDescricao())
                .createdAt(model.getCreatedAt())
                .build();
    }

    private PermissaoModel toDomain(PermissaoEntity entity) {
        if (entity == null) return null;

        return PermissaoModel.builder()
                .id(entity.getId())
                .codigo(entity.getCodigo())
                .descricao(entity.getDescricao())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    // ===== OPERAÇÕES =====

    @Override
    public PermissaoModel save(PermissaoModel model) {
        Objects.requireNonNull(model, "PermissaoModel não pode ser nulo");

        PermissaoEntity entity = toEntity(model);
        PermissaoEntity saved = repository.save(entity);

        return toDomain(saved);
    }

    @Override
    public Optional<PermissaoModel> findById(UUID id) {
        Objects.requireNonNull(id, "id não pode ser nulo");

        return repository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public List<PermissaoModel> findAll() {
        return repository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        Objects.requireNonNull(id, "id não pode ser nulo");

        repository.deleteById(id);
    }

    @Override
    public boolean existsByCodigo(String codigo) {
        Objects.requireNonNull(codigo, "codigo não pode ser nulo");

        return repository.existsByCodigo(codigo);
    }

    @Override
    public Optional<PermissaoModel> findByCodigo(String codigo) {
        Objects.requireNonNull(codigo, "codigo não pode ser nulo");

        return repository.findByCodigoIgnoreCase(codigo)
                .map(this::toDomain);
    }
}