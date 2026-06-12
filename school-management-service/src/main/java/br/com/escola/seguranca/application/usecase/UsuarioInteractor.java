package br.com.escola.seguranca.application.usecase;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.institucional.adapter.out.persistence.entity.EscolaEntity;
import br.com.escola.institucional.adapter.out.persistence.repository.EscolaJpaRepository;
import br.com.escola.institucional.application.service.EscolaTenantService;
import br.com.escola.seguranca.adapter.out.persistence.entity.UsuarioEntity;
import br.com.escola.seguranca.adapter.out.persistence.mapper.UsuarioMapper;
import br.com.escola.seguranca.application.port.in.UsuarioUseCasePort;
import br.com.escola.seguranca.application.port.out.UsuarioRepositoryPort;
import br.com.escola.seguranca.domain.model.UsuarioModel;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioInteractor implements UsuarioUseCasePort {

    private final UsuarioRepositoryPort repository;
    private final UsuarioMapper mapper;
    private final EscolaJpaRepository escolaJpaRepository;
    private final EscolaTenantService escolaTenantService;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    // ================= CREATE =================
    @Override
    @Transactional
    public UsuarioModel create(UsuarioModel model) {
        validar(model);

        if (repository.findByUsername(model.getUsername()).isPresent()) {
            throw new DataIntegrityViolationException("Já existe um usuário com este username.");
        }

        if (repository.existsByEmail(model.getEmail())) {
            throw new DataIntegrityViolationException("Já existe um usuário com este email.");
        }

        if (model.getSenhaHash() != null) {
            model.setSenhaHash(passwordEncoder.encode(model.getSenhaHash()));
        }

        UsuarioEntity entity = mapper.toEntity(model);
        entity.setEscola(resolverEscola(model.getEscolaId()));
        UsuarioEntity saved = repository.save(entity);

        return mapper.toDomain(saved);
    }

    // ================= UPDATE =================
    @Override
    @Transactional
    public UsuarioModel update(UUID id, UsuarioModel model) {
        validar(model);

        UsuarioEntity existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        if (repository.existsByUsernameAndIdNot(model.getUsername(), id)) {
            throw new DataIntegrityViolationException("Já existe um usuário com este username.");
        }

        if (repository.existsByEmailAndIdNot(model.getEmail(), id)) {
            throw new DataIntegrityViolationException("Já existe um usuário com este email.");
        }

        existing.setUsername(model.getUsername().trim());
        existing.setNome(model.getNome());
        existing.setEmail(model.getEmail().trim());
        existing.setAtivo(model.isAtivo());
        existing.setEscola(resolverEscola(model.getEscolaId()));

        if (model.getSenhaHash() != null && !model.getSenhaHash().isBlank()) {
            existing.setSenhaHash(passwordEncoder.encode(model.getSenhaHash()));
        }

        existing.getPerfis().clear();
        existing.getPerfis().addAll(mapper.toEntity(model).getPerfis());

        return mapper.toDomain(repository.save(existing));
    }

    // ================= DELETE =================
    @Override
    @Transactional
    public void delete(UUID id) {
        if (id == null) throw new IllegalArgumentException("ID nulo");
        if (!repository.existsById(id)) throw new IllegalArgumentException("Usuário não encontrado.");
        repository.deleteById(id);
    }

    // ================= FIND BY ID =================
    @Override
    @Transactional(readOnly = true)
    public UsuarioModel findById(UUID id) {
        return repository.findById(id)
                .map(mapper::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
    }

    // ================= FIND BY USERNAME =================
    @Override
    @Transactional(readOnly = true)
    public UsuarioModel findByUsername(String username) {
        return repository.findByUsername(username)
                .map(mapper::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
    }

    // ================= LIST =================
    @Override
    @Transactional(readOnly = true)
    public List<UsuarioModel> listAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    // ================= EXISTS =================
    @Override
    public boolean existsByUsername(String username) {
        return repository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    // ================= VALIDAÇÃO =================
    private void validar(UsuarioModel model) {
        Objects.requireNonNull(model);

        if (model.getUsername() == null || model.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username obrigatório");
        }

        if (model.getEmail() == null || model.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email obrigatório");
        }

        if (model.getPerfis() == null || model.getPerfis().isEmpty()) {
            throw new IllegalArgumentException("Usuário deve possuir ao menos um perfil.");
        }
    }

    private EscolaEntity resolverEscola(UUID escolaId) {
        if (escolaId == null) {
            return escolaTenantService.obterOuCriarEscolaPadrao();
        }

        return escolaJpaRepository.findById(escolaId)
                .orElseThrow(() -> new IllegalArgumentException("Escola não encontrada."));
    }
}
