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

import br.com.escola.identityaccessservice.application.dto.UsuarioRequest;
import br.com.escola.identityaccessservice.application.dto.UsuarioResponse;
import br.com.escola.identityaccessservice.application.model.UsuarioAdministrado;
import br.com.escola.identityaccessservice.application.port.in.AdministrarUsuarioUseCase;
import jakarta.validation.Valid;

@RestController
@RequestMapping({ "/internal/v1/usuarios", "/internal/usuarios" })
public class AdministracaoUsuarioController {

    private final AdministrarUsuarioUseCase administrarUsuarioUseCase;

    public AdministracaoUsuarioController(AdministrarUsuarioUseCase administrarUsuarioUseCase) {
        this.administrarUsuarioUseCase = administrarUsuarioUseCase;
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return administrarUsuarioUseCase.listar().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public UsuarioResponse buscar(@PathVariable UUID id) {
        return toResponse(administrarUsuarioUseCase.buscar(id));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(administrarUsuarioUseCase.criar(request)));
    }

    @PutMapping("/{id}")
    public UsuarioResponse atualizar(@PathVariable UUID id, @Valid @RequestBody UsuarioRequest request) {
        return toResponse(administrarUsuarioUseCase.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        administrarUsuarioUseCase.excluir(id);
        return ResponseEntity.noContent().build();
    }

    private UsuarioResponse toResponse(UsuarioAdministrado usuario) {
        Set<UUID> perfilIds = usuario.perfis().stream()
                .map(perfil -> perfil.id())
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        return new UsuarioResponse(
                usuario.id(),
                usuario.username(),
                usuario.username(),
                usuario.nome(),
                usuario.email(),
                usuario.ativo(),
                usuario.escolaId(),
                usuario.createdAt(),
                perfilIds,
                perfilIds,
                usuario.perfis());
    }
}
