package br.com.escola.accesscontrol.application.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import br.com.escola.accesscontrol.adapter.in.web.dto.AuthResponse;
import br.com.escola.accesscontrol.adapter.out.persistence.entity.SessaoAutenticacaoEntity;
import br.com.escola.accesscontrol.adapter.out.persistence.entity.UsuarioEntity;
import br.com.escola.accesscontrol.adapter.out.persistence.repository.SessaoAutenticacaoJpaRepository;
import br.com.escola.accesscontrol.adapter.out.persistence.repository.UsuarioJpaRepository;
import br.com.escola.accesscontrol.domain.exception.CredenciaisInvalidasException;
import br.com.escola.accesscontrol.domain.exception.TokenInvalidoOuExpiradoException;

@Service
public class AuthService {

    private static final int REFRESH_DIAS = 7;
    private static final int ACCESS_MINUTOS = 30;

    private final UsuarioJpaRepository usuarioRepository;
    private final SessaoAutenticacaoJpaRepository sessaoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    public AuthService(
            UsuarioJpaRepository usuarioRepository,
            SessaoAutenticacaoJpaRepository sessaoRepository,
            PasswordEncoder passwordEncoder,
            JdbcTemplate jdbcTemplate) {
        this.usuarioRepository = usuarioRepository;
        this.sessaoRepository = sessaoRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    public AuthResponse login(String login, String senha) {
        UsuarioEntity usuario = usuarioRepository.findByUsernameIgnoreCaseAndAtivoTrue(login)
                .or(() -> usuarioRepository.findByEmailIgnoreCaseAndAtivoTrue(login))
                .orElseThrow(() -> new CredenciaisInvalidasException("Usuário ou senha inválidos"));

        if (!isValidPassword(senha, usuario.getSenhaHash(), usuario.getId())) {
            throw new CredenciaisInvalidasException("Usuário ou senha inválidos");
        }


        if ("admin".equalsIgnoreCase(usuario.getUsername())) {
            garantirVinculoAdmin(usuario.getId());
        }

        String accessToken = gerarToken();
        String refreshToken = gerarToken();

        sessaoRepository.save(new SessaoAutenticacaoEntity(
                usuario,
                hashToken(refreshToken),
                hashToken(accessToken),
                LocalDateTime.now().plusDays(REFRESH_DIAS),
                LocalDateTime.now().plusMinutes(ACCESS_MINUTOS)));

        return new AuthResponse(accessToken, refreshToken, "Bearer", usuario.getUsername(), usuario.getNome(),
                usuarioRepository.findPerfisByIdUsuario(usuario.getId()),
                usuarioRepository.findPermissoesByIdUsuario(usuario.getId()));
    }


    private void garantirVinculoAdmin(UUID idUsuario) {
        Integer total = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM usuario_perfil up
                JOIN perfil p ON p.id_perfil = up.id_perfil
                WHERE up.id_usuario = ?
                  AND p.codigo = 'ADMIN'
                """, Integer.class, idUsuario);

        if (total != null && total > 0) {
            return;
        }

        UUID idUsuarioPerfil = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO usuario_perfil (id_usuario_perfil, id_usuario, id_perfil)
                SELECT ?, ?, p.id_perfil
                FROM perfil p
                WHERE p.codigo = 'ADMIN'
                """, idUsuarioPerfil, idUsuario);
    }

    public AuthResponse refresh(String refreshToken) {
        SessaoAutenticacaoEntity sessao = sessaoRepository
                .findByRefreshTokenHashAndRevogadoFalseAndExpiraEmAfter(hashToken(refreshToken), LocalDateTime.now())
                .orElseThrow(() -> new TokenInvalidoOuExpiradoException("Refresh token inválido ou expirado"));

        String newAccessToken = gerarToken();
        String newRefreshToken = gerarToken();

        sessao.renovar(
                hashToken(newRefreshToken),
                hashToken(newAccessToken),
                LocalDateTime.now().plusDays(REFRESH_DIAS),
                LocalDateTime.now().plusMinutes(ACCESS_MINUTOS));
        sessaoRepository.save(sessao);

        UsuarioEntity usuario = sessao.getUsuario();
        return new AuthResponse(newAccessToken, newRefreshToken, "Bearer", usuario.getUsername(), usuario.getNome(),
                usuarioRepository.findPerfisByIdUsuario(usuario.getId()),
                usuarioRepository.findPermissoesByIdUsuario(usuario.getId()));
    }

    public void logout(String refreshToken) {
        SessaoAutenticacaoEntity sessao = sessaoRepository
                .findByRefreshTokenHashAndRevogadoFalseAndExpiraEmAfter(hashToken(refreshToken), LocalDateTime.now())
                .orElseThrow(() -> new TokenInvalidoOuExpiradoException("Sessão não encontrada para logout"));

        sessao.revogar();
        sessaoRepository.save(sessao);
    }

    public UsuarioEntity validarAccessToken(String accessToken) {
        SessaoAutenticacaoEntity sessao = sessaoRepository
                .findByAccessTokenHashAndRevogadoFalseAndAccessExpiraEmAfter(hashToken(accessToken), LocalDateTime.now())
                .orElseThrow(() -> new TokenInvalidoOuExpiradoException("Access token inválido ou expirado"));

        return sessao.getUsuario();
    }

    public List<String> buscarPermissoes(UUID idUsuario) {
        return usuarioRepository.findPermissoesByIdUsuario(idUsuario);
    }


    private boolean isValidPassword(String senhaInformada, String senhaHashOuLegada, UUID idUsuario) {
        if (senhaHashOuLegada == null || senhaHashOuLegada.isBlank()) {
            return false;
        }

        String senhaHashNormalizada = senhaHashOuLegada.trim();
        boolean pareceBcrypt = senhaHashNormalizada.startsWith("$2a$")
                || senhaHashNormalizada.startsWith("$2b$")
                || senhaHashNormalizada.startsWith("$2y$");
        if (pareceBcrypt) {
            return passwordEncoder.matches(senhaInformada, senhaHashNormalizada);
        }

        if (!senhaInformada.equals(senhaHashNormalizada)) {
            return false;
        }

        String novoHash = passwordEncoder.encode(senhaInformada);
        jdbcTemplate.update("UPDATE usuario SET senha_hash = ? WHERE id_usuario = ?", novoHash, idUsuario);
        return true;
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
