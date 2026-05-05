package br.com.escola.accesscontrol.adapter.in.web.controller;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import br.com.escola.accesscontrol.adapter.in.web.dto.UsuarioRequest;
import br.com.escola.accesscontrol.adapter.in.web.dto.UsuarioResponse;
import br.com.escola.accesscontrol.application.port.in.UsuarioUseCasePort;
import br.com.escola.accesscontrol.domain.model.PerfilModel;
import br.com.escola.accesscontrol.domain.model.UsuarioModel;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Gerenciamento de usuários")
@Validated
@CrossOrigin(origins = "http://localhost:4200")
public class UsuarioController {

    private final UsuarioUseCasePort usuarioUseCasePort;

    // ========================= DTO =========================
    private UsuarioResponse toDto(UsuarioModel m) {
        return new UsuarioResponse(
                m.getId(),
                m.getUsername(),
                m.getNome(),
                m.getEmail(),
                m.isAtivo(),
                m.getCreatedAt()
        );
    }

    // ========================= PERFIS =========================
    private Set<PerfilModel> validarEConverterPerfis(List<UUID> perfisIds) {
        if (perfisIds == null || perfisIds.isEmpty()) {
            throw new IllegalArgumentException("Não é possível criar/atualizar usuário sem perfis.");
        }

        return perfisIds.stream().map(pid -> {
            if (pid == null) {
                throw new IllegalArgumentException("perfilId não pode ser nulo");
            }
            PerfilModel perfil = new PerfilModel();
            perfil.setId(pid);
            return perfil;
        }).collect(Collectors.toSet());
    }

    // ========================= CREATE =========================
    @PostMapping
    @PreAuthorize("hasAnyAuthority('CREATE','ADMIN')")
    @Operation(summary = "Cria usuário")
    public ResponseEntity<UsuarioResponse> create(@Validated @RequestBody UsuarioRequest dto) {

        Set<PerfilModel> perfis = validarEConverterPerfis(dto.perfilIds());

        UsuarioModel domain = UsuarioModel.builder()
                .username(dto.username())
                .nome(dto.nome())
                .email(dto.email())
                .senhaHash(dto.senhaHash())
                .ativo(dto.ativo() == null || dto.ativo())
                .perfis(perfis)
                .build();

        try {
            UsuarioModel created = usuarioUseCasePort.create(domain);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(created));
        } catch (DataIntegrityViolationException ex) {
            throw new DataIntegrityViolationException("Já existe usuário com username/email.", ex);
        }
    }

    // ========================= UPDATE =========================
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('UPDATE','ADMIN')")
    @Operation(summary = "Atualiza usuário")
    public ResponseEntity<UsuarioResponse> update(@PathVariable UUID id,
                                                  @Validated @RequestBody UsuarioRequest dto) {

        Set<PerfilModel> perfis = validarEConverterPerfis(dto.perfilIds());

        UsuarioModel domain = UsuarioModel.builder()
                .id(id)
                .username(dto.username())
                .nome(dto.nome())
                .email(dto.email())
                .senhaHash(dto.senhaHash())
                .ativo(dto.ativo() == null || dto.ativo())
                .perfis(perfis)
                .build();

        try {
            UsuarioModel updated = usuarioUseCasePort.update(id, domain);
            return ResponseEntity.ok(toDto(updated));
        } catch (DataIntegrityViolationException ex) {
            throw new DataIntegrityViolationException("Já existe usuário com username/email.", ex);
        }
    }

    // ========================= DELETE =========================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('DELETE','ADMIN')")
    @Operation(summary = "Remove usuário")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        usuarioUseCasePort.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ========================= FIND BY ID =========================
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('READ','ADMIN')")
    @Operation(summary = "Busca usuário por ID")
    public ResponseEntity<UsuarioResponse> findById(@PathVariable UUID id) {
        UsuarioModel usuario = usuarioUseCasePort.findById(id);
        return ResponseEntity.ok(toDto(usuario));
    }

    // ========================= LIST =========================
    @GetMapping
    @PreAuthorize("hasAnyAuthority('READ_ALL','ADMIN')")
    @Operation(summary = "Lista usuários")
    public ResponseEntity<List<UsuarioResponse>> listAll() {
        List<UsuarioResponse> list = usuarioUseCasePort.listAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }
}