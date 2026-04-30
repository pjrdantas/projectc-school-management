package br.com.escola.accesscontrol.adapter.in.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.accesscontrol.adapter.out.persistence.entity.PerfilEntity;
import br.com.escola.accesscontrol.adapter.out.persistence.repository.PerfilJpaRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/perfis")
public class PerfilController {
    private final PerfilJpaRepository repository;
    private final JdbcTemplate jdbcTemplate;

    public PerfilController(PerfilJpaRepository repository, JdbcTemplate jdbcTemplate) {
        this.repository = repository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PerfilResponse criar(@Valid @RequestBody PerfilRequest request) {
        if (repository.existsByCodigo(request.codigo())) throw new IllegalArgumentException("Código já cadastrado");
        PerfilEntity created = repository.save(new PerfilEntity(null, request.codigo().trim(), request.nome().trim(), request.descricao()));
        vincularPermissoes(created.getId(), request.permissaoIds());
        return toResponse(created);
    }

    @GetMapping("/{id}")
    public PerfilResponse buscarPorId(@PathVariable java.util.UUID id) {
        var entity = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Perfil não encontrado"));
        return toResponse(entity);
    }

    @GetMapping
    public List<PerfilResponse> listar() { return repository.findAll().stream().map(this::toResponse).toList(); }

    @PutMapping("/{id}")
    public PerfilResponse atualizar(@PathVariable java.util.UUID id, @Valid @RequestBody PerfilRequest request) {
        PerfilEntity entity = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Perfil não encontrado"));
        repository.delete(entity);
        PerfilEntity novo = repository.save(new PerfilEntity(id, request.codigo().trim(), request.nome().trim(), request.descricao()));
        vincularPermissoes(novo.getId(), request.permissaoIds());
        return toResponse(novo);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable java.util.UUID id) { repository.deleteById(id); }

    private void vincularPermissoes(UUID idPerfil, List<UUID> permissaoIds){
        jdbcTemplate.update("DELETE FROM perfil_permissao WHERE id_perfil = ?", idPerfil);
        if (permissaoIds == null) return;
        for (UUID idPermissao : permissaoIds){
            jdbcTemplate.update("INSERT INTO perfil_permissao (id_perfil_permissao,id_perfil,id_permissao) VALUES (?,?,?)", UUID.randomUUID(), idPerfil, idPermissao);
        }
    }

    private PerfilResponse toResponse(PerfilEntity e) { return new PerfilResponse(e.getId(), e.getCodigo(), e.getNome(), e.getDescricao(), e.getCreatedAt()); }
}
