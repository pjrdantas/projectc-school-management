package br.com.projeto.piloto.adapter.in.web.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

import br.com.projeto.piloto.adapter.in.web.dto.AuthPerfilRequestDTO;
import br.com.projeto.piloto.adapter.in.web.dto.AuthPerfilResponseDTO;
import br.com.projeto.piloto.adapter.in.web.dto.AuthPermissaoResponseDTO;
import br.com.projeto.piloto.adapter.in.web.exception.ErrorResponse;
import br.com.projeto.piloto.adapter.out.jpa.mapper.AuthPerfilMapper;
import br.com.projeto.piloto.domain.model.AuthPerfilModel;
import br.com.projeto.piloto.domain.port.inbound.AuthPerfilUseCase;
import br.com.projeto.piloto.domain.port.inbound.AuthPermissaoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/perfis")
@RequiredArgsConstructor
@Tag(name = "Perfis", description = "Gerenciamento de perfis")
@Validated
@CrossOrigin(origins = "http://localhost:4200")
public class AuthPerfilController {

    private final AuthPerfilUseCase authPerfilUseCase;
    private final AuthPermissaoUseCase authPermissaoUseCase;
 

   
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
    @PreAuthorize("hasAuthority('CREATE')")
    @Operation(summary = "Cria um novo perfil", responses = {
        @ApiResponse(responseCode = "201", description = "Perfil criado com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthPerfilResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos",           content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Perfil já existe",          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "422", description = "Erro de validação",         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<?> create(@Validated @RequestBody AuthPerfilRequestDTO dto, HttpServletRequest request) {
    	   	
        if (authPerfilUseCase.existsByNmPerfil(dto.nmPerfil())) {
            return buildErrorResponse(HttpStatus.CONFLICT, "Perfil já existe: " + dto.nmPerfil(), request);
        }

        

        var permissoes = Optional.ofNullable(dto.permissoesIds())
            .orElse(Set.of())
            .stream()
            .map(id -> authPermissaoUseCase.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Permissão não encontrada: " + id)))
            .collect(Collectors.toSet());

        AuthPerfilModel domain = AuthPerfilMapper.toDomain(dto, permissoes);
        AuthPerfilModel created = authPerfilUseCase.create(domain);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AuthPerfilMapper.toResponse(created));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE')")
    @Operation(summary = "Atualiza um perfil existente", responses = {
            @ApiResponse(responseCode = "200", description = "Atualização realizada com sucesso",  content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthPerfilResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Perfil não encontrado",              content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<?> update(@PathVariable("id") Long id, @Validated @RequestBody AuthPerfilRequestDTO dto, HttpServletRequest request) {
 
        authPerfilUseCase.findById(id).orElseThrow(() -> new IllegalArgumentException("Perfil não encontrado: " + id));

        var permissoes = Optional.ofNullable(dto.permissoesIds())
            .orElse(Set.of())
            .stream()
            .map(pid -> authPermissaoUseCase.findById(pid)
            .orElseThrow(() -> new IllegalArgumentException("Permissão não encontrada: " + pid)))
            .collect(Collectors.toSet());

        AuthPerfilModel domain = AuthPerfilMapper.toDomain(dto, permissoes);
        AuthPerfilModel updated = authPerfilUseCase.update(id, domain);

        return ResponseEntity.ok(AuthPerfilMapper.toResponse(updated));
    }    
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE')")
    @Operation(summary = "Remove um perfil", responses = {
            @ApiResponse(responseCode = "204", description = "Removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Perfil não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<?> delete(@PathVariable("id") Long id, HttpServletRequest request) {
    	
        Optional<AuthPerfilModel> existing = authPerfilUseCase.findById(id);
        if (existing.isPresent()) {
            authPerfilUseCase.delete(id);
            return ResponseEntity.noContent().build();  
        } else {
            return buildErrorResponse(HttpStatus.NOT_FOUND, "Perfil não encontrada: " + id, request);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ')")
    @Operation(summary = "Busca perfil por id", responses = {
            @ApiResponse(responseCode = "200", description = "Perfil encontrada",     content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthPermissaoResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Perfil não encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<?> findById(@PathVariable("id") Long id, HttpServletRequest request) {
    	
        Optional<AuthPerfilModel> existing = authPerfilUseCase.findById(id);
        if (existing.isPresent()) {
            AuthPerfilResponseDTO dto = AuthPerfilMapper.toResponse(existing.get());
            return ResponseEntity.ok(dto);
        } else {
            return buildErrorResponse(HttpStatus.NOT_FOUND, "Perfil não encontrada: " + id, request);
        }
    }

    @GetMapping
    @PreAuthorize("hasAuthority('READ_ALL')")
    @Operation(summary = "Lista todos os perfis")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",  content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthPermissaoResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Lista não encontrada",         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<?> listAll(HttpServletRequest request) {
    	
        List<AuthPerfilModel> domains = authPerfilUseCase.listAll();
        if (domains.isEmpty()) {
            return buildErrorResponse(HttpStatus.NOT_FOUND, "Nenhum Perfil encontrado", request);
        }
        List<AuthPerfilResponseDTO> list = domains.stream().map(AuthPerfilMapper::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

}
