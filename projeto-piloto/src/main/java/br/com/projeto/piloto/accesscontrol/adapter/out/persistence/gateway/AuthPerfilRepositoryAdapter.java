package br.com.projeto.piloto.accesscontrol.adapter.out.persistence.gateway;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.projeto.piloto.accesscontrol.adapter.out.persistence.entity.AuthPerfil;
import br.com.projeto.piloto.accesscontrol.adapter.out.persistence.mapper.AuthPerfilMapper;
import br.com.projeto.piloto.accesscontrol.adapter.out.persistence.mapper.AuthPermissaoMapper;
import br.com.projeto.piloto.accesscontrol.adapter.out.persistence.repository.SpringAuthPerfilRepository;
import br.com.projeto.piloto.accesscontrol.application.port.out.AuthPerfilRepositoryPort;
import br.com.projeto.piloto.accesscontrol.domain.model.AuthPerfilModel;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthPerfilRepositoryAdapter implements AuthPerfilRepositoryPort {

    private final SpringAuthPerfilRepository repository;

    @Override
    public @NonNull List<AuthPerfilModel> listAll() {
        List<AuthPerfilModel> list = repository.findAll().stream()
                .map(AuthPerfilMapper::toDomain)
                .collect(Collectors.toList());
        

        return Objects.requireNonNull(list); 
    }


    @Override
    public Optional<AuthPerfilModel> findById(Long id) {
        Objects.requireNonNull(id, "ID não pode ser nulo");
        return repository.findById(id).map(AuthPerfilMapper::toDomain);
    }

    @SuppressWarnings({ "null" })
	@Override
    @Transactional
    public @NonNull AuthPerfilModel create(@NonNull AuthPerfilModel domain) {
        AuthPerfil entity = AuthPerfilMapper.toEntity(domain);
        AuthPerfil saved = repository.save(entity);
        return Objects.requireNonNull(AuthPerfilMapper.toDomain(saved));
    }

    @Override
    @Transactional
    public @NonNull AuthPerfilModel update(@NonNull Long id, @NonNull AuthPerfilModel domain) {
        Objects.requireNonNull(id, "ID não pode ser nulo");
        Objects.requireNonNull(domain, "AuthPerfilModel não pode ser nulo"); 

        AuthPerfilModel updatedModel = repository.findById(id)
                .map(existing -> {
                    existing.setNmPerfil(domain.getNmPerfil());

                    existing.getPermissoes().clear();
                    
                    if (domain.getPermissoes() != null) {
                        existing.getPermissoes().addAll(
                            domain.getPermissoes().stream()
                                .map(AuthPermissaoMapper::toEntity)
                                .collect(Collectors.toSet())
                        );
                    }

                    AuthPerfil saved = Objects.requireNonNull(repository.save(existing));
                    return AuthPerfilMapper.toDomain(saved);
                })
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado: " + id));

        return Objects.requireNonNull(updatedModel);
    }

    @Override
    public void delete(@NonNull Long id) {
        Objects.requireNonNull(id, "ID não pode ser nulo");
        repository.deleteById(id);
    }


    @Override
    public boolean existsByNmPerfil(String nmPerfil) {
        return repository.existsByNmPerfil(nmPerfil);
    }

    @Override
    public Optional<AuthPerfilModel> findByNmPerfil(String nome) {
        return repository.findByNmPerfil(nome).map(AuthPerfilMapper::toDomain);
    }
}
