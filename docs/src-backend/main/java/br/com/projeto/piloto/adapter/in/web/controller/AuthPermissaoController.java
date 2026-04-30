package br.com.projeto.piloto.adapter.in.web.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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

import br.com.projeto.piloto.adapter.in.web.dto.AuthPermissaoRequestDTO;
import br.com.projeto.piloto.adapter.in.web.dto.AuthPermissaoResponseDTO;
import br.com.projeto.piloto.adapter.in.web.exception.ErrorResponse;
import br.com.projeto.piloto.adapter.out.jpa.mapper.AuthPermissaoMapper;
import br.com.projeto.piloto.domain.model.AuthPermissaoModel;
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
@RequestMapping("/api/permissoes")
@RequiredArgsConstructor
@Tag(name = "Permissões", description = "Gerenciamento de permissões")
@Validated
@CrossOrigin(origins = "http://localhost:4200")
public class AuthPermissaoController {

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
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('CREATE')")
    @Operation(summary = "Cria uma nova permissão", responses = {
            @ApiResponse(responseCode = "201", description = "Permissão criada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthPermissaoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",              content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Permissão já existe",          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Erro de validação",            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<?> create(@Validated @RequestBody AuthPermissaoRequestDTO dto, HttpServletRequest request) {

        if (authPermissaoUseCase.existsByNmPermissao(dto.nmPermissao())) {
            return buildErrorResponse(HttpStatus.CONFLICT,
                    "Permissão já existe com o nome: " + dto.nmPermissao(), request);
        }

        AuthPermissaoModel domain = AuthPermissaoMapper.toDomain(dto);
        AuthPermissaoModel created = authPermissaoUseCase.create(domain);
        AuthPermissaoResponseDTO response = AuthPermissaoMapper.toResponse(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
        
	@PutMapping("/{id}")
	@PreAuthorize("hasAuthority('UPDATE')")
	@Operation(summary = "Atualiza uma permissão existente", responses = {
			@ApiResponse(responseCode = "200", description = "Atualização realizada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthPermissaoResponseDTO.class))),
			@ApiResponse(responseCode = "404", description = "Permissão não encontrada",          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))) })
	public ResponseEntity<?> update(@PathVariable("id") Long id, @Validated @RequestBody AuthPermissaoRequestDTO dto, HttpServletRequest request) {

	    Optional<AuthPermissaoModel> existing = authPermissaoUseCase.findById(id);
	    if (existing.isPresent()) {
	        AuthPermissaoModel domain = AuthPermissaoMapper.toDomain(dto);
	        AuthPermissaoResponseDTO updatedDto = AuthPermissaoMapper.toResponse(authPermissaoUseCase.update(id, domain));
	        return ResponseEntity.ok(updatedDto);
	    } else {
	        return buildErrorResponse(HttpStatus.NOT_FOUND, "Permissão não encontrada: " + id, request);
	    }
	}


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE')")
    @Operation(summary = "Remove uma permissão", responses = {
            @ApiResponse(responseCode = "204", description = "Removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Permissão não encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<?> delete(@PathVariable("id") Long id, HttpServletRequest request) {
    	
        Optional<AuthPermissaoModel> existing = authPermissaoUseCase.findById(id);
        if (existing.isPresent()) {
            authPermissaoUseCase.delete(id);
            return ResponseEntity.noContent().build();  
        } else {
            return buildErrorResponse(HttpStatus.NOT_FOUND, "Permissão não encontrada: " + id, request);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ')")
    @Operation(summary = "Busca permissão por id", responses = {
            @ApiResponse(responseCode = "200", description = "Permissão encontrada",     content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthPermissaoResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Permissão não encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<?> findById(@PathVariable("id") Long id, HttpServletRequest request) {
    	
        Optional<AuthPermissaoModel> existing = authPermissaoUseCase.findById(id);
        if (existing.isPresent()) {
            AuthPermissaoResponseDTO dto = AuthPermissaoMapper.toResponse(existing.get());
            return ResponseEntity.ok(dto);
        } else {
            return buildErrorResponse(HttpStatus.NOT_FOUND, "Permissão não encontrada: " + id, request);
        }
    }
    
    @GetMapping
    @PreAuthorize("hasAuthority('READ_ALL')")
    @Operation(summary = "Lista todas as permissões")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",  content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthPermissaoResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Lista não encontrada",         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<?> listAll(HttpServletRequest request) {
    	
        List<AuthPermissaoModel> domains = authPermissaoUseCase.listAll();
        if (domains.isEmpty()) {
            return buildErrorResponse(HttpStatus.NOT_FOUND, "Nenhuma permissão encontrada", request);
        }
        List<AuthPermissaoResponseDTO> list = domains.stream().map(AuthPermissaoMapper::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

}
