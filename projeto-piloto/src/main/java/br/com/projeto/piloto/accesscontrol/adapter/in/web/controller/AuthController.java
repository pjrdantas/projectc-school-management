package br.com.projeto.piloto.accesscontrol.adapter.in.web.controller;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.projeto.piloto.accesscontrol.adapter.in.web.dto.AuthResponseDTO;
import br.com.projeto.piloto.accesscontrol.adapter.in.web.dto.LoginRequestDTO;
import br.com.projeto.piloto.accesscontrol.adapter.in.web.dto.RefreshTokenRequestDTO;
import br.com.projeto.piloto.accesscontrol.application.service.AuthSessaoService;
import br.com.projeto.piloto.accesscontrol.application.usecase.AuthInteractor;
import br.com.projeto.piloto.accesscontrol.domain.model.AuthUsuarioModel;
import br.com.projeto.piloto.accesscontrol.infrastructure.security.JwtUtil;
import br.com.projeto.piloto.shared.exception.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Gerenciamento de login, registro e refresh de tokens")
@Validated
@CrossOrigin(origins = "http://localhost:4200")
@Slf4j
public class AuthController {

	private final AuthInteractor authInteractor;
	private final JwtUtil jwtUtil;
	private final AuthSessaoService authSessaoService;

	@PostMapping("/login")
	@Operation(summary = "Login do usuário")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Login realizado com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(example = "{ \"token\": \"access-token\", \"refreshToken\": \"refresh-token\", \"login\": \"usuario\" }"))),
			@ApiResponse(responseCode = "401", description = "Credenciais inválidas",       content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))) })
	public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {
	    AuthUsuarioModel authUsuario = authInteractor.authenticate(request.login(), request.senha());
	    
	    Set<String> roles = new HashSet<>();
	    Set<String> permissions = new HashSet<>();
	    
	    authUsuario.getPerfis().forEach(perfil -> {
	  
	       
	        String nomePerfil = perfil.getNmPerfil();
	        if (nomePerfil != null && !nomePerfil.isBlank()) {
	            roles.add(nomePerfil.toUpperCase());
	        }

	        if (perfil.getPermissoes() != null) {
	            perfil.getPermissoes().forEach(p -> {
	                if (p != null && p.getNmPermissao() != null) {
	                    permissions.add(p.getNmPermissao().toUpperCase());
	                }
	            });
	        }
	    });

	    Set<String> authorities = new HashSet<>(roles);
	    authorities.addAll(permissions);

	    String token = jwtUtil.generateToken(authUsuario.getLogin(), authorities);
	    String refreshToken = jwtUtil.generateRefreshToken(authUsuario.getLogin());

	    authSessaoService.criarSessao(authUsuario.getId(), token, refreshToken);

	    return ResponseEntity.ok(new AuthResponseDTO(
	        token, 
	        refreshToken, 
	        authUsuario.getLogin(), 
	        authUsuario.getNome(), 
	        roles,
	        permissions
	    ));
	}

	@PostMapping("/validate-session")
	@Operation(summary = "Valida se a sessão atual é válida")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Sessão é válida"),
			@ApiResponse(responseCode = "401", description = "Sessão inválida ou expirada") })
	public ResponseEntity<?> validateSession(@RequestBody Map<String, String> request) {
		try {
			String token = request.get("token");
			
			if (token == null || token.isBlank()) {
				return ResponseEntity.badRequest()
						.body(Map.of("valid", false, "message", "Token não fornecido"));
			}

			boolean sessaoValida = authSessaoService.validarSessao(token);
			
			if (!sessaoValida) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("valid", false, "message", "Sessão inválida ou expirada. Faça login novamente."));
			}

			return ResponseEntity.ok(Map.of("valid", true));

		} catch (Exception e) {

			log.error("Erro ao validar sessão: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("valid", false, "message", "Sessão inválida."));
		}
	}

	@PostMapping("/refresh-token")
	@Operation(summary = "Renova o access token usando refresh token")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Novo access token gerado", content = @Content(mediaType = "application/json", schema = @Schema(example = "{ \"accessToken\": \"novo-access-token\" }"))),
			@ApiResponse(responseCode = "400", description = "Token inválido ou argumento inválido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "401", description = "Token expirado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "500", description = "Erro interno", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))) })
	public ResponseEntity<?> refresh(@RequestBody RefreshTokenRequestDTO request) {

		String refreshToken = request.refreshToken();
		
		if (refreshToken == null || refreshToken.isBlank()) {
			return ResponseEntity.badRequest()
					.body(ErrorResponse.builder().timestamp(LocalDateTime.now())
					.status(HttpStatus.BAD_REQUEST.value())
					.error(HttpStatus.BAD_REQUEST.getReasonPhrase())
					.message("O token de refresh não pode ser nulo ou vazio")
					.path("/api/auth/refresh-token")
					.build());
		}

		try {
			if (!jwtUtil.validate(refreshToken)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(ErrorResponse.builder()
							.timestamp(LocalDateTime.now())
							.status(HttpStatus.UNAUTHORIZED.value())
							.error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
							.message("Refresh token inválido ou expirado")
							.path("/api/auth/refresh-token")
							.build());
			}

			var sessao = authSessaoService.encontrarPorRefreshToken(refreshToken);
			if (sessao.isEmpty()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(ErrorResponse.builder()
							.timestamp(LocalDateTime.now())
							.status(HttpStatus.UNAUTHORIZED.value())
							.error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
							.message("Sessão inválida ou expirada. Faça login novamente.")
							.path("/api/auth/refresh-token")
							.build());
			}

			String login = jwtUtil.extractUsernameFromRefreshToken(refreshToken);

			AuthUsuarioModel authUsuario = authInteractor.findByLogin(login);
			
			Set<String> roles = authUsuario.getPerfis().stream()
					.map(p -> p.getNmPerfil())
					.filter(n -> n != null && !n.isBlank())
					.map(String::toUpperCase)
					.collect(Collectors.toSet());
			
			Set<String> permissions = new HashSet<>();
			authUsuario.getPerfis().forEach(perfil -> {
				if (perfil.getPermissoes() != null) {
					perfil.getPermissoes().forEach(p -> {
						if (p != null && p.getNmPermissao() != null) {
							permissions.add(p.getNmPermissao().toUpperCase());
						}
					});
				}
			});

			Set<String> authorities = new HashSet<>(roles);
			authorities.addAll(permissions);

			String newAccessToken = jwtUtil.generateToken(login, authorities);

			return ResponseEntity.ok(Map.of("accessToken", newAccessToken));

		} catch (ExpiredJwtException ex) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(ErrorResponse.builder()
					.timestamp(LocalDateTime.now())
					.status(HttpStatus.UNAUTHORIZED.value())
					.error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
					.message("O token de refresh expirou")
					.path("/api/auth/refresh-token").build());
			
		} catch (MalformedJwtException ex) {
			return ResponseEntity.badRequest()
					.body(ErrorResponse.builder()
					.timestamp(LocalDateTime.now())
					.status(HttpStatus.BAD_REQUEST.value())
					.error(HttpStatus.BAD_REQUEST.getReasonPhrase())
					.message("O token de refresh fornecido é inválido ou malformado")
					.path("/api/auth/refresh-token").build());
						
		} catch (JwtException ex) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ErrorResponse.builder()
					.timestamp(LocalDateTime.now())
					.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
					.error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
					.message("Erro ao processar o token de refresh: " + ex.getMessage())
					.path("/api/auth/refresh-token")
					.build());
		}
	}
}