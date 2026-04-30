package br.com.escola.accesscontrol.adapter.in.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.accesscontrol.adapter.out.persistence.entity.UsuarioEntity;
import br.com.escola.accesscontrol.adapter.out.persistence.repository.UsuarioJpaRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioJpaRepository repository;
    private final JdbcTemplate jdbcTemplate;

    public UsuarioController(UsuarioJpaRepository repository, JdbcTemplate jdbcTemplate) {
        this.repository = repository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse criar(@Valid @RequestBody UsuarioRequest request) {
        if (repository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Já existe usuário com este username");
        }
        if (repository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Já existe usuário com este email");
        }

        UsuarioEntity created = repository.save(new UsuarioEntity(
                null,
                request.username().trim(),
                request.nome().trim(),
                request.email().trim().toLowerCase(),
                request.senhaHash().trim(),
                request.ativo() == null || request.ativo()));

        vincularPerfis(created.getId(), request.perfilIds());
        return toResponse(created);
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public UsuarioResponse buscarPorId(@PathVariable java.util.UUID id) {
        UsuarioEntity entity = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        return toResponse(entity);
    }

    @PutMapping("/{id}")
    public UsuarioResponse atualizar(@PathVariable java.util.UUID id, @Valid @RequestBody UsuarioRequest request) {
        UsuarioEntity entity = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        UsuarioEntity atualizado = repository.save(new UsuarioEntity(
                entity.getId(),
                request.username().trim(),
                request.nome().trim(),
                request.email().trim().toLowerCase(),
                request.senhaHash().trim(),
                request.ativo() == null || request.ativo()));
        vincularPerfis(atualizado.getId(), request.perfilIds());
        return toResponse(atualizado);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable java.util.UUID id) {
        repository.deleteById(id);
    }

    private void vincularPerfis(UUID idUsuario, List<UUID> perfilIds){
        jdbcTemplate.update("DELETE FROM usuario_perfil WHERE id_usuario = ?", idUsuario);
        if (perfilIds == null) return;
        for (UUID idPerfil : perfilIds){
            jdbcTemplate.update("INSERT INTO usuario_perfil (id_usuario_perfil,id_usuario,id_perfil) VALUES (?,?,?)", UUID.randomUUID(), idUsuario, idPerfil);
        }
    }

    private UsuarioResponse toResponse(UsuarioEntity entity) {
        return new UsuarioResponse(
                entity.getId(),
                entity.getUsername(),
                entity.getNome(),
                entity.getEmail(),
                entity.isAtivo(),
                entity.getCreatedAt());
    }
}
