package br.com.escola.accesscontrol.adapter.in.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import br.com.escola.accesscontrol.adapter.out.persistence.entity.PermissaoEntity;
import br.com.escola.accesscontrol.adapter.out.persistence.repository.PermissaoJpaRepository;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/permissoes")
public class PermissaoController {
    private final PermissaoJpaRepository repository;
    public PermissaoController(PermissaoJpaRepository repository) { this.repository = repository; }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public PermissaoResponse criar(@Valid @RequestBody PermissaoRequest r) {
        if (repository.existsByCodigo(r.codigo())) throw new IllegalArgumentException("Código já cadastrado");
        return toResponse(repository.save(new PermissaoEntity(null, r.codigo().trim(), r.descricao())));
    }
    @GetMapping public List<PermissaoResponse> listar() { return repository.findAll().stream().map(this::toResponse).toList(); }
    @PutMapping("/{id}")
    public PermissaoResponse atualizar(@PathVariable UUID id, @Valid @RequestBody PermissaoRequest r) {
        repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Permissão não encontrada"));
        return toResponse(repository.save(new PermissaoEntity(id, r.codigo().trim(), r.descricao())));
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable UUID id) { repository.deleteById(id); }
    private PermissaoResponse toResponse(PermissaoEntity e){ return new PermissaoResponse(e.getId(), e.getCodigo(), e.getDescricao(), e.getCreatedAt()); }
}
