package br.com.escola.accesscontrol.application.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.escola.accesscontrol.adapter.in.web.auth.AuthResponse;
import br.com.escola.accesscontrol.adapter.out.persistence.entity.SessaoAutenticacaoEntity;
import br.com.escola.accesscontrol.adapter.out.persistence.entity.UsuarioEntity;
import br.com.escola.accesscontrol.adapter.out.persistence.repository.SessaoAutenticacaoJpaRepository;
import br.com.escola.accesscontrol.adapter.out.persistence.repository.UsuarioJpaRepository;

@Service
public class AuthService {

    private static final int REFRESH_DIAS = 7;
    private static final int ACCESS_MINUTOS = 30;

    private final UsuarioJpaRepository usuarioRepository;
    private final SessaoAutenticacaoJpaRepository sessaoRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UsuarioJpaRepository usuarioRepository,
            SessaoAutenticacaoJpaRepository sessaoRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.sessaoRepository = sessaoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse login(String login, String senha) {
        UsuarioEntity usuario = usuarioRepository.findByUsernameIgnoreCaseAndAtivoTrue(login)
                .or(() -> usuarioRepository.findByEmailIgnoreCaseAndAtivoTrue(login))
                .orElseThrow(() -> new IllegalArgumentException("Usuário ou senha inválidos"));

        if (!passwordEncoder.matches(senha, usuario.getSenhaHash())) {
            throw new IllegalArgumentException("Usuário ou senha inválidos");
        }

        String accessToken = gerarToken();
        String refreshToken = gerarToken();

        sessaoRepository.save(new SessaoAutenticacaoEntity(
                usuario,
                hashToken(refreshToken),
                hashToken(accessToken),
                LocalDateTime.now().plusDays(REFRESH_DIAS),
                LocalDateTime.now().plusMinutes(ACCESS_MINUTOS)));

        return new AuthResponse(accessToken, refreshToken, "Bearer", usuario.getUsername(), usuario.getNome());
    }

    public AuthResponse refresh(String refreshToken) {
        SessaoAutenticacaoEntity sessao = sessaoRepository
                .findByRefreshTokenHashAndRevogadoFalseAndExpiraEmAfter(hashToken(refreshToken), LocalDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException("Refresh token inválido ou expirado"));

        String newAccessToken = gerarToken();
        String newRefreshToken = gerarToken();

        sessao.renovar(
                hashToken(newRefreshToken),
                hashToken(newAccessToken),
                LocalDateTime.now().plusDays(REFRESH_DIAS),
                LocalDateTime.now().plusMinutes(ACCESS_MINUTOS));
        sessaoRepository.save(sessao);

        UsuarioEntity usuario = sessao.getUsuario();
        return new AuthResponse(newAccessToken, newRefreshToken, "Bearer", usuario.getUsername(), usuario.getNome());
    }

    public void logout(String refreshToken) {
        SessaoAutenticacaoEntity sessao = sessaoRepository
                .findByRefreshTokenHashAndRevogadoFalseAndExpiraEmAfter(hashToken(refreshToken), LocalDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException("Sessão não encontrada para logout"));

        sessao.revogar();
        sessaoRepository.save(sessao);
    }

    public UsuarioEntity validarAccessToken(String accessToken) {
        SessaoAutenticacaoEntity sessao = sessaoRepository
                .findByAccessTokenHashAndRevogadoFalseAndAccessExpiraEmAfter(hashToken(accessToken), LocalDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException("Access token inválido ou expirado"));

        return sessao.getUsuario();
    }

    public List<String> buscarPermissoes(UUID idUsuario) {
        return usuarioRepository.findPermissoesByIdUsuario(idUsuario);
    }

    private String gerarToken() {
        return UUID.randomUUID() + "." + UUID.randomUUID();
    }

    private String hashToken(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Algoritmo de hash indisponível", ex);
        }
    }
}
