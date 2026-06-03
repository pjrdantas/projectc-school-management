package br.com.escola.seguranca.adapter.in.web.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.seguranca.adapter.in.web.dto.PerfilRequest;
import br.com.escola.seguranca.adapter.in.web.dto.PerfilResponse;
import br.com.escola.seguranca.adapter.out.persistence.mapper.PerfilMapper;
import br.com.escola.seguranca.application.port.in.PerfilUseCasePort;
import br.com.escola.seguranca.application.port.in.PermissaoUseCasePort;
import br.com.escola.seguranca.domain.model.PerfilModel;
import br.com.escola.compartilhado.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/perfis")
@RequiredArgsConstructor
@Tag(name = "Perfis", description = "Gerenciamento de perfis")
@Validated
@CrossOrigin(origins = "http://localhost:4200")
public class PerfilController {

    private final PerfilUseCasePort perfilUseCasePort;
    private final PermissaoUseCasePort permissaoUseCase;

    private ResponseEntity<ErrorResponse> buildErrorResponse(@NonNull HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(status.value())
                        .error(status.getReasonPhrase())
                        .message(message)
                        .path(request.getRequestURI())
                        .build());
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('CREATE','ADMIN')")
    @Operation(summary = "Cria um novo perfil")
    public ResponseEntity<?> create(@Validated @RequestBody PerfilRequest dto, HttpServletRequest request) {

        if (perfilUseCasePort.existsByCodigo(dto.codigo())) {
            return buildErrorResponse(HttpStatus.CONFLICT, "Perfil já existe: " + dto.codigo(), request);
        }

        var permissoes = Optional.ofNullable(dto.permissoesIds())
                .orElse(Set.of())
                .stream()
                .map(id -> permissaoUseCase.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Permissão não encontrada: " + id)))
                .collect(Collectors.toSet());

        PerfilModel domain = PerfilMapper.toDomain(dto, permissoes);
        PerfilModel created = perfilUseCasePort.create(domain);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PerfilMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('UPDATE','ADMIN')")
    @Operation(summary = "Atualiza um perfil")
    public ResponseEntity<?> update(@PathVariable UUID id,
                                    @Validated @RequestBody PerfilRequest dto,
                                    HttpServletRequest request) {

        perfilUseCasePort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Perfil não encontrado: " + id));

        var permissoes = Optional.ofNullable(dto.permissoesIds())
                .orElse(Set.of())
                .stream()
                .map(pid -> permissaoUseCase.findById(pid)
                        .orElseThrow(() -> new IllegalArgumentException("Permissão não encontrada: " + pid)))
                .collect(Collectors.toSet());

        PerfilModel domain = PerfilMapper.toDomain(dto, permissoes);
        PerfilModel updated = perfilUseCasePort.update(id, domain);

        return ResponseEntity.ok(PerfilMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('DELETE','ADMIN')")
    @Operation(summary = "Remove um perfil")
    public ResponseEntity<?> delete(@PathVariable UUID id, HttpServletRequest request) {

        Optional<PerfilModel> existing = perfilUseCasePort.findById(id);
        if (existing.isPresent()) {
            perfilUseCasePort.delete(id);
            return ResponseEntity.noContent().build();
        }

        return buildErrorResponse(HttpStatus.NOT_FOUND, "Perfil não encontrado: " + id, request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('READ','ADMIN')")
    @Operation(summary = "Busca perfil por ID")
    public ResponseEntity<?> findById(@PathVariable UUID id, HttpServletRequest request) {

        Optional<PerfilModel> existing = perfilUseCasePort.findById(id);
        if (existing.isPresent()) {
            return ResponseEntity.ok(PerfilMapper.toResponse(existing.get()));
        }

        return buildErrorResponse(HttpStatus.NOT_FOUND, "Perfil não encontrado: " + id, request);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('READ_ALL','ADMIN')")
    @Operation(summary = "Lista perfis")
    public ResponseEntity<?> listAll(HttpServletRequest request) {

        List<PerfilModel> domains = perfilUseCasePort.listAll();

        if (domains.isEmpty()) {
            return buildErrorResponse(HttpStatus.NOT_FOUND, "Nenhum Perfil encontrado", request);
        }

        List<PerfilResponse> list = domains.stream()
                .map(PerfilMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }
}
