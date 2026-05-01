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

import br.com.projeto.piloto.adapter.in.web.dto.AplicativosRequestDTO;
import br.com.projeto.piloto.adapter.in.web.dto.AplicativosResponseDTO;
import br.com.projeto.piloto.adapter.in.web.dto.AuthPermissaoResponseDTO;
import br.com.projeto.piloto.adapter.in.web.exception.ErrorResponse;
import br.com.projeto.piloto.adapter.out.jpa.mapper.AplicativosMapper;
import br.com.projeto.piloto.domain.model.AplicativosModel;
import br.com.projeto.piloto.domain.port.inbound.AplicativosUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/aplicativos")
@RequiredArgsConstructor
@Tag(name = "Aplicativos", description = "Gerenciamento de aplicativos")
@Validated
@CrossOrigin(origins = "http://localhost:4200")
public class AplicativosController {

    private final AplicativosUseCase aplicativosUseCase;

    private ResponseEntity<ErrorResponse> buildErrorResponse(@NonNull HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(
                        ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(status.value())
                                .error(status.getReasonPhrase())
                                .message(message)
                                .path(request.getRequestURI())
                                .build());
    }

    
    @PostMapping
    @PreAuthorize("hasAuthority('CREATE')")
    @Operation(summary = "Cria um novo Aplicativo", responses = {
            @ApiResponse(responseCode = "201", description = "Aplicativo criado com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthPermissaoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",               content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Aplicativo já existe",          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Erro de validação",             content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<?> create(@Validated @RequestBody AplicativosRequestDTO dto, HttpServletRequest request) {

        if (aplicativosUseCase.existsByNome(dto.nome())) {
            return buildErrorResponse(HttpStatus.CONFLICT,
                    "Aplicativo já existe com o nome: " + dto.nome(),request);
        }

        AplicativosModel domain = AplicativosMapper.toDomain(dto);
        AplicativosModel created = aplicativosUseCase.create(domain);
        AplicativosResponseDTO response = AplicativosMapper.toResponse(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE')")
    @Operation(summary = "Atualiza um Aplicativo existente", responses = {
			@ApiResponse(responseCode = "200", description = "Atualização realizada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthPermissaoResponseDTO.class))),
			@ApiResponse(responseCode = "404", description = "Aplicativo não encontrado",         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))) })
    public ResponseEntity<?> update(@PathVariable("id") Long id, @Validated @RequestBody AplicativosRequestDTO dto, HttpServletRequest request) {

	    Optional<AplicativosModel> existing = aplicativosUseCase.findById(id);
	    if (existing.isPresent()) {
	        AplicativosModel domain = AplicativosMapper.toDomain(dto);
	        AplicativosResponseDTO updatedDto = AplicativosMapper.toResponse(aplicativosUseCase.update(id, domain));
	        return ResponseEntity.ok(updatedDto);
	    } else {
	        return buildErrorResponse(HttpStatus.NOT_FOUND, "Aplicativo não encontrado: " + id, request);
	    }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE')")
    @Operation(summary = "Remove um Aplicativo", responses = {
            @ApiResponse(responseCode = "204", description = "Removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Aplicativo não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<?> delete(@PathVariable("id") Long id, HttpServletRequest request) {

        Optional<AplicativosModel> existing = aplicativosUseCase.findById(id);
       if (existing.isPresent()) {
    	   aplicativosUseCase.delete(id);
           return ResponseEntity.noContent().build();  
       } else {
           return buildErrorResponse(HttpStatus.NOT_FOUND, "Aplicativo não encontrado: " + id, request);
       }
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ')")
    @Operation(summary = "Busca Aplicativo por id", responses = {
            @ApiResponse(responseCode = "200", description = "Aplicativo encontrado",     content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthPermissaoResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Aplicativo não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<?> findById(@PathVariable("id") Long id, HttpServletRequest request) {

        Optional<AplicativosModel> existing = aplicativosUseCase.findById(id);
        if (existing.isPresent()) {
            AplicativosResponseDTO dto = AplicativosMapper.toResponse(existing.get());
            return ResponseEntity.ok(dto);
        } else {
            return buildErrorResponse(HttpStatus.NOT_FOUND, "Aplicativo não encontrado: " + id, request);
        }
    }
    
    @GetMapping
    @PreAuthorize("hasAuthority('READ_ALL')")
    @Operation(summary = "Lista todos os Aplicativos")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",  content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthPermissaoResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Lista não encontrada",         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<List<AplicativosResponseDTO>> listAll() {
        List<AplicativosModel> domains = aplicativosUseCase.listAll();
        List<AplicativosResponseDTO> response = domains.stream()
                .map(AplicativosMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);

    }

    
    @GetMapping("/ativos")
    @PreAuthorize("hasAuthority('READ_ACTIVE')")
    @Operation(summary = "Lista somente os aplicativos ativos")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",  content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthPermissaoResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Lista não encontrada",         content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<?> listAtivos(HttpServletRequest request) {

        List<AplicativosModel> domains = aplicativosUseCase.listAtivos();     
        List<AplicativosResponseDTO> response = domains.stream()
                .map(AplicativosMapper::toResponse)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(response);
    }


}
