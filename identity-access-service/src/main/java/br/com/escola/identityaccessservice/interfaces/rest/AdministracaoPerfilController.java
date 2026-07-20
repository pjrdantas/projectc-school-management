package br.com.escola.identityaccessservice.interfaces.rest;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.identityaccessservice.application.dto.PerfilRequest;
import br.com.escola.identityaccessservice.application.dto.PerfilResponse;
import br.com.escola.identityaccessservice.application.model.PerfilAdministrado;
import br.com.escola.identityaccessservice.application.port.in.AdministrarPerfilUseCase;
import jakarta.validation.Valid;

@RestController
@RequestMapping({ "/internal/v1/perfis", "/internal/perfis" })
public class AdministracaoPerfilController {

    private final AdministrarPerfilUseCase administrarPerfilUseCase;

    public AdministracaoPerfilController(AdministrarPerfilUseCase administrarPerfilUseCase) {
        this.administrarPerfilUseCase = administrarPerfilUseCase;
    }

    @GetMapping
    public List<PerfilResponse> listar() {
        return administrarPerfilUseCase.listar().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public PerfilResponse buscar(@PathVariable UUID id) {
        return toResponse(administrarPerfilUseCase.buscar(id));
    }

    @PostMapping
    public ResponseEntity<PerfilResponse> criar(@Valid @RequestBody PerfilRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(administrarPerfilUseCase.criar(request)));
    }

    @PutMapping("/{id}")
    public PerfilResponse atualizar(@PathVariable UUID id, @Valid @RequestBody PerfilRequest request) {
        return toResponse(administrarPerfilUseCase.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        administrarPerfilUseCase.excluir(id);
        return ResponseEntity.noContent().build();
    }

    private PerfilResponse toResponse(PerfilAdministrado perfil) {
        Set<UUID> permissaoIds = perfil.permissoes().stream()
                .map(permissao -> permissao.id())
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        return new PerfilResponse(
                perfil.id(),
                perfil.codigo(),
                perfil.nome(),
                perfil.nome(),
                perfil.descricao(),
                perfil.createdAt(),
                permissaoIds,
                permissaoIds,
                perfil.permissoes());
    }
}
