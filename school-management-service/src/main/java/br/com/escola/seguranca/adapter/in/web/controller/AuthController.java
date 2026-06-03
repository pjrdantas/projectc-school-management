package br.com.escola.seguranca.adapter.in.web.controller;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.seguranca.adapter.in.web.dto.AuthRequest;
import br.com.escola.seguranca.adapter.in.web.dto.AuthResponse;
import br.com.escola.seguranca.adapter.in.web.dto.LogoutRequest;
import br.com.escola.seguranca.adapter.in.web.dto.RefreshRequest;
import br.com.escola.seguranca.application.service.AuthService;
import br.com.escola.compartilhado.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Gerenciamento de login e tokens")
@Validated
@CrossOrigin(origins = "http://localhost:4200")
@Slf4j
public class AuthController {

    private final AuthService authService;

    // ================= LOGIN =================
    @PostMapping("/login")
    @Operation(summary = "Login do usuário")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        try {
            AuthResponse response = authService.login(
                    request.login().trim(),
                    request.senha().trim()
            );
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(buildError("Credenciais inválidas", "/api/auth/login"));
        }
    }

    // ================= REFRESH =================
    @PostMapping("/refresh")
    @Operation(summary = "Refresh do token")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshRequest request) {

        if (request.refreshToken() == null || request.refreshToken().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(buildError("Refresh token não pode ser vazio", "/api/auth/refresh"));
        }

        try {
            AuthResponse response = authService.refresh(request.refreshToken().trim());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(buildError("Refresh token inválido", "/api/auth/refresh"));
        }
    }

    // ================= LOGOUT =================
    @PostMapping("/logout")
    @Operation(summary = "Logout do usuário")
    public ResponseEntity<?> logout(@Valid @RequestBody LogoutRequest request) {

        if (request.refreshToken() == null || request.refreshToken().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(buildError("Refresh token não pode ser vazio", "/api/auth/logout"));
        }

        authService.logout(request.refreshToken().trim());

        return ResponseEntity.noContent().build();
    }

    // ================= ERROR =================
    private ErrorResponse buildError(String message, String path) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(message)
                .path(path)
                .build();
    }
}