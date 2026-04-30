package br.com.projeto.piloto.adapter.out.jpa;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.projeto.piloto.adapter.out.jpa.entity.AuthPermissao;
import br.com.projeto.piloto.adapter.out.jpa.repository.SpringAuthPermissaoRepository;
import br.com.projeto.piloto.domain.model.AuthPermissaoModel;
import br.com.projeto.piloto.domain.port.outbound.AuthPermissaoRepositoryPort;
import lombok.RequiredArgsConstructor;

@Component
@Transactional
@RequiredArgsConstructor
public class AuthPermissaoRepositoryAdapter implements AuthPermissaoRepositoryPort {

    private final SpringAuthPermissaoRepository permissaoRepository;

    private AuthPermissao toEntity(AuthPermissaoModel model) {
        if (model == null) return null;
        AuthPermissao entity = new AuthPermissao();
        entity.setId(model.getId());
        entity.setNmPermissao(model.getNmPermissao());
        return entity;
    }

    private AuthPermissaoModel toDomain(AuthPermissao entity) {
        if (entity == null) return null;
        return AuthPermissaoModel.builder()
                .id(entity.getId())
                .nmPermissao(entity.getNmPermissao())
                .build();
    }

    @SuppressWarnings("null")
	@Override
    public AuthPermissaoModel save(AuthPermissaoModel model) {
        Objects.requireNonNull(model, "AuthPermissaoModel não pode ser nulo");
        AuthPermissao entity = toEntity(model);
        AuthPermissao saved = permissaoRepository.save(entity);  
        return toDomain(saved);
    }

    @Override
    public Optional<AuthPermissaoModel> findById(Long id) {
        Objects.requireNonNull(id, "id não pode ser nulo");
        return permissaoRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public List<AuthPermissaoModel> findAll() {
        return permissaoRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        Objects.requireNonNull(id, "id não pode ser nulo");
        permissaoRepository.deleteById(id);
    }

    @Override
    public boolean existsByNmPermissao(String nmPermissao) {
        Objects.requireNonNull(nmPermissao, "nmPermissao não pode ser nulo");
        return permissaoRepository.existsByNmPermissao(nmPermissao);
    }
}
