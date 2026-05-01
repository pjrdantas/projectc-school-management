
package br.com.projeto.piloto.adapter.in.web.controller;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import br.com.projeto.piloto.adapter.in.web.dto.AuthPerfilResumoDTO;
import br.com.projeto.piloto.adapter.in.web.dto.AuthUsuarioRequestDTO;
import br.com.projeto.piloto.adapter.in.web.dto.AuthUsuarioResponseDTO;
import br.com.projeto.piloto.adapter.in.web.exception.ErrorResponse;
import br.com.projeto.piloto.domain.exception.UserNotFoundException;
import br.com.projeto.piloto.domain.model.AuthPerfilModel;
import br.com.projeto.piloto.domain.model.AuthUsuarioModel;
import br.com.projeto.piloto.domain.port.inbound.AuthUsuarioUseCasePort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Gerenciamento de usuários do sistema")
@Validated
@CrossOrigin(origins = "http://localhost:4200")
public class UsuarioController {

    private final AuthUsuarioUseCasePort usuarioUseCase;

    private AuthUsuarioResponseDTO toDto(AuthUsuarioModel m) {
        Set<AuthPerfilResumoDTO> perfisIdsDto = m.getPerfis().stream()
                .map(p -> new AuthPerfilResumoDTO(
                        p.getId(),
                        p.getNmPerfil()
                ))
                .collect(Collectors.toSet());

        return new AuthUsuarioResponseDTO(
                m.getId(),
                m.getLogin(),
                m.getNome(),
                m.getAtivo(),
                m.getEmail(),
                m.getCriadoEm(),
                m.getAtualizadoEm(),
                perfisIdsDto
        );
    }

    private Set<AuthPerfilModel> validarEConverterPerfis(Set<Long> perfisIds) {
        if (perfisIds == null || perfisIds.isEmpty()) {
            throw new IllegalArgumentException("Não é possível criar ou alterar usuário sem perfis. 'perfisIds' não pode ser nulo ou vazio.");
        }

        return perfisIds.stream().map(pid -> {
            if (pid == null) {
                throw new IllegalArgumentException("Não é possível criar ou alterar usuário com 'perfisIds' contendo id nulo.");
            }
            AuthPerfilModel perfil = new AuthPerfilModel();
            perfil.setId(pid);
            return perfil;
        }).collect(Collectors.toSet());
    }

    private String normalizarAtivo(String ativo) {
        if (ativo == null || (!ativo.equalsIgnoreCase("S") && !ativo.equalsIgnoreCase("N"))) {
            return "N";
        }
        return ativo.toUpperCase();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE')")
    @Operation(summary = "Cria um novo usuário", responses = {
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthUsuarioResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Usuário já existe", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Erro de validação", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<AuthUsuarioResponseDTO> create(@Validated @RequestBody AuthUsuarioRequestDTO dto) {

        Set<AuthPerfilModel> perfis = validarEConverterPerfis(dto.perfisIds());

        AuthUsuarioModel domain = AuthUsuarioModel.builder()
                .login(dto.login())
                .senha(dto.senha())
                .nome(dto.nome())
                .ativo(normalizarAtivo(dto.ativo()))
                .email(dto.email())
                .perfis(perfis)
                .build();

        try {
            AuthUsuarioModel created = usuarioUseCase.criar(domain);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(created));
        } catch (DataIntegrityViolationException ex) {
            throw new DataIntegrityViolationException("Já existe um usuário com este login.", ex);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE')")
    @Operation(summary = "Atualiza um usuário existente", responses = {
            @ApiResponse(responseCode = "200", description = "Atualização realizada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthUsuarioResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<AuthUsuarioResponseDTO> update(@PathVariable("id") Long id, @Validated @RequestBody AuthUsuarioRequestDTO dto) {

        Set<AuthPerfilModel> perfis = validarEConverterPerfis(dto.perfisIds());

        AuthUsuarioModel domain = AuthUsuarioModel.builder()
                .id(id)
                .login(dto.login())
                .senha(dto.senha())
                .nome(dto.nome())
                .ativo(normalizarAtivo(dto.ativo()))
                .email(dto.email())
                .perfis(perfis)
                .build();

        try {
            AuthUsuarioModel updated = usuarioUseCase.atualizar(id, domain);
            return ResponseEntity.ok(toDto(updated));
        } catch (UserNotFoundException ex) {
            throw new UserNotFoundException("Usuário com ID " + id + " não encontrado.");
        } catch (DataIntegrityViolationException ex) {
            throw new DataIntegrityViolationException("Já existe um usuário com este login.", ex);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE')")
    @Operation(summary = "Remove um usuário", responses = {
            @ApiResponse(responseCode = "204", description = "Removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        usuarioUseCase.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ')")
    @Operation(summary = "Busca um usuário por ID", responses = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthUsuarioResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<AuthUsuarioResponseDTO> findById(@PathVariable("id") Long id) {
        AuthUsuarioModel usuario = usuarioUseCase.buscarPorId(id);
        return ResponseEntity.ok(toDto(usuario));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('READ_ALL')")
    @Operation(summary = "Lista todos os usuários")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthUsuarioResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Lista não encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<List<AuthUsuarioResponseDTO>> listAll() {
        List<AuthUsuarioResponseDTO> list = usuarioUseCase.listarTodos().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }
}
