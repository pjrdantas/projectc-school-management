package br.com.escola.accesscontrol.adapter.in.web;

import java.util.List;

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
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/perfis")
public class PerfilController {
    private final PerfilJpaRepository repository;

    public PerfilController(PerfilJpaRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PerfilResponse criar(@Valid @RequestBody PerfilRequest request) {
        if (repository.existsByCodigo(request.codigo())) throw new IllegalArgumentException("Código já cadastrado");
        return toResponse(repository.save(new PerfilEntity(null, request.codigo().trim(), request.nome().trim(), request.descricao())));
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
        return toResponse(novo);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable java.util.UUID id) { repository.deleteById(id); }

    private PerfilResponse toResponse(PerfilEntity e) { return new PerfilResponse(e.getId(), e.getCodigo(), e.getNome(), e.getDescricao(), e.getCreatedAt()); }
}
