package br.com.escola.accesscontrol.adapter.in.web.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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

import br.com.escola.accesscontrol.adapter.in.web.dto.PermissaoRequest;
import br.com.escola.accesscontrol.adapter.out.persistence.mapper.PermissaoMapper;
import br.com.escola.accesscontrol.application.port.in.PermissaoUseCasePort;
import br.com.escola.accesscontrol.domain.model.PermissaoModel;
import br.com.escola.shared.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/permissoes")
@RequiredArgsConstructor
@Tag(name = "Permissões", description = "Gerenciamento de permissões")
@Validated
@CrossOrigin(origins = "http://localhost:4200")
public class PermissaoController {

    private static final String PERMISSAO_ADMIN = "ADMIN";

    private final PermissaoUseCasePort useCase;

    // ===== ERROR PADRÃO =====
    private ResponseEntity<ErrorResponse> buildErrorResponse(
            @NonNull HttpStatus status,
            String message,
            HttpServletRequest request) {

        return ResponseEntity.status(status)
                .body(ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(status.value())
                        .error(status.getReasonPhrase())
                        .message(message)
                        .path(request.getRequestURI())
                        .build());
    }

    // ===== CREATE =====
    @PostMapping
    @PreAuthorize("hasAnyAuthority('CREATE','ADMIN')")
    @Operation(summary = "Cria uma nova permissão")
    public ResponseEntity<?> create(@Valid @RequestBody PermissaoRequest dto,
                                    HttpServletRequest request) {

        if (useCase.existsByCodigo(dto.codigo())) {
            return buildErrorResponse(HttpStatus.CONFLICT,
                    "Permissão já existe: " + dto.codigo(), request);
        }

        PermissaoModel domain = PermissaoMapper.toDomain(dto);
        PermissaoModel created = useCase.create(domain);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PermissaoMapper.toResponse(created));
    }

    // ===== UPDATE =====
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('UPDATE','ADMIN')")
    @Operation(summary = "Atualiza uma permissão")
    public ResponseEntity<?> update(@PathVariable UUID id,
                                    @Valid @RequestBody PermissaoRequest dto,
                                    HttpServletRequest request) {

        Optional<PermissaoModel> existing = useCase.findById(id);

        if (existing.isEmpty()) {
            return buildErrorResponse(HttpStatus.NOT_FOUND,
                    "Permissão não encontrada: " + id, request);
        }

        // 🔥 proteção ADMIN
        if (PERMISSAO_ADMIN.equalsIgnoreCase(existing.get().getCodigo()) &&
                !PERMISSAO_ADMIN.equalsIgnoreCase(dto.codigo())) {

            return buildErrorResponse(HttpStatus.CONFLICT,
                    "A permissão ADMIN não pode ser alterada", request);
        }

        PermissaoModel domain = PermissaoMapper.toDomain(dto);
        PermissaoModel updated = useCase.update(id, domain);

        return ResponseEntity.ok(PermissaoMapper.toResponse(updated));
    }

    // ===== DELETE =====
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('DELETE','ADMIN')")
    public ResponseEntity<?> delete(@PathVariable UUID id,
                                    HttpServletRequest request) {

        Optional<PermissaoModel> existing = useCase.findById(id);

        if (existing.isEmpty()) {
            return buildErrorResponse(HttpStatus.NOT_FOUND,
                    "Permissão não encontrada: " + id, request);
        }

        // 🔥 proteção ADMIN
        if (PERMISSAO_ADMIN.equalsIgnoreCase(existing.get().getCodigo())) {
            return buildErrorResponse(HttpStatus.CONFLICT,
                    "A permissão ADMIN não pode ser removida", request);
        }

        useCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ===== FIND BY ID =====
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('READ','ADMIN')")
    public ResponseEntity<?> findById(@PathVariable UUID id,
                                     HttpServletRequest request) {

        Optional<PermissaoModel> existing = useCase.findById(id);

        if (existing.isEmpty()) {
            return buildErrorResponse(HttpStatus.NOT_FOUND,
                    "Permissão não encontrada: " + id, request);
        }

        return ResponseEntity.ok(
                PermissaoMapper.toResponse(existing.get())
        );
    }

    // ===== LIST =====
    @GetMapping
    @PreAuthorize("hasAnyAuthority('READ_ALL','ADMIN')")
    public ResponseEntity<?> listAll(HttpServletRequest request) {

        List<PermissaoModel> list = useCase.listAll();

        if (list.isEmpty()) {
            return buildErrorResponse(HttpStatus.NOT_FOUND,
                    "Nenhuma permissão encontrada", request);
        }

        return ResponseEntity.ok(
                list.stream()
                        .map(PermissaoMapper::toResponse)
                        .collect(Collectors.toList())
        );
    }
}