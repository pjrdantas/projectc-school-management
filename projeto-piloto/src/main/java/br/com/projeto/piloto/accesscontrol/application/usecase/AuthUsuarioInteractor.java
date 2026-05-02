package br.com.projeto.piloto.accesscontrol.application.usecase;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.projeto.piloto.accesscontrol.adapter.out.persistence.entity.AuthUsuario;
import br.com.projeto.piloto.accesscontrol.adapter.out.persistence.mapper.AuthUsuarioMapper;
import br.com.projeto.piloto.accesscontrol.application.port.in.AuthUsuarioUseCasePort;
import br.com.projeto.piloto.accesscontrol.application.port.out.AuthUsuarioRepositoryPort;
import br.com.projeto.piloto.accesscontrol.domain.exception.UserNotFoundException;
import br.com.projeto.piloto.accesscontrol.domain.model.AuthUsuarioModel;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthUsuarioInteractor implements AuthUsuarioUseCasePort {

    private final AuthUsuarioRepositoryPort repository;
    private final AuthUsuarioMapper mapper;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    
    @Override
    @Transactional 
    public AuthUsuarioModel criar(AuthUsuarioModel model) {
        validar(model);

        if (repository.findByLogin(model.getLogin()).isPresent()) {
            throw new DataIntegrityViolationException("Já existe um usuário com este login.");
        }

        if (model.getSenha() != null) {
            model.setSenha(passwordEncoder.encode(model.getSenha()));
        }

        AuthUsuario entity = mapper.toEntity(model);
        AuthUsuario saved = repository.save(entity);
        return mapper.toDomain(saved);
    }


    @Override
    @Transactional
    public AuthUsuarioModel atualizar(Long id, AuthUsuarioModel model) {
        validar(model);

        AuthUsuario existing = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuário com ID " + id + " não encontrado."));

        if (repository.existsByDsLoginAndIdNot(model.getLogin(), id)) {
            throw new DataIntegrityViolationException("Já existe um usuário com este login.");
        }

        existing.setLogin(model.getLogin());
        existing.setNome(model.getNome());
        existing.setAtivo(model.getAtivo());
        existing.setEmail(model.getEmail());

        if (model.getSenha() != null && !model.getSenha().isBlank()) {
            existing.setSenha(passwordEncoder.encode(model.getSenha()));
        }


        existing.getPerfis().clear();
        existing.getPerfis().addAll(mapper.toEntity(model).getPerfis());

        return mapper.toDomain(repository.save(existing));
    }

    @Override
    @Transactional
    public void deletar(Long id) {
        if (id == null) throw new IllegalArgumentException("ID nulo");
        if (!repository.existsById(id)) throw new UserNotFoundException("Usuário não encontrado.");
        repository.deleteById(id);
    }    

    @Override
    public AuthUsuarioModel buscarPorId(Long id) {
        return repository.findById(id)
                .map(mapper::toDomain)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado."));
    }

    @Override
    public List<AuthUsuarioModel> listarTodos() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    private void validar(AuthUsuarioModel model) {
        Objects.requireNonNull(model);
        if (model.getPerfis() == null || model.getPerfis().isEmpty()) {
            throw new IllegalArgumentException("Usuário deve possuir ao menos um perfil.");
        }
    }
}