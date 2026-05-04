package br.com.escola.accesscontrol.adapter.in.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.accesscontrol.adapter.in.web.dto.AuthRequest;
import br.com.escola.accesscontrol.adapter.in.web.dto.AuthResponse;
import br.com.escola.accesscontrol.adapter.in.web.dto.LogoutRequest;
import br.com.escola.accesscontrol.adapter.in.web.dto.RefreshRequest;
import br.com.escola.accesscontrol.application.service.AuthService;
import br.com.escola.accesscontrol.domain.exception.JwtUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        return authService.login(request.login().trim(), request.senha().trim());
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request.refreshToken().trim());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request.refreshToken().trim());
    }
}
