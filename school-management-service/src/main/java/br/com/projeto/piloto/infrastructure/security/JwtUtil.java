package br.com.projeto.piloto.infrastructure.security;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import br.com.projeto.piloto.infrastructure.config.AuthProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final AuthProperties authProperties;

    public JwtUtil(AuthProperties authProperties) {
        this.authProperties = authProperties;

        String secret = authProperties.getJwt().getSecret();

        // 🔥 GARANTE TAMANHO MÍNIMO (>= 32 bytes)
        byte[] keyBytes = resolveKey(secret);

        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Resolve a chave garantindo tamanho mínimo e suporte a Base64
     */
    private byte[] resolveKey(String secret) {
        byte[] keyBytes;

        try {
            // tenta Base64 primeiro (melhor prática)
            keyBytes = Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException e) {
            // fallback: usa string pura
            keyBytes = secret.getBytes();
        }

        // 🔥 valida tamanho mínimo (32 bytes = 256 bits)
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                "JWT secret muito fraco. Deve ter no mínimo 32 bytes (256 bits)."
            );
        }

        return keyBytes;
    }

    public String generateToken(String username, Set<String> authorities) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .setSubject(username)
                .claim("authorities", authorities.stream().toList())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + authProperties.getJwt().getExpirationMs()))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(String username) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + authProperties.getJwt().getRefreshExpirationMs()))
                .signWith(key)
                .compact();
    }

    public boolean validate(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException ex) {
            return false;
        }
    }

    public String getUsername(String token) {
        return parse(token).getSubject();
    }

    public List<String> getAuthorities(String token) {
        Object auths = parse(token).get("authorities");

        if (auths instanceof List<?> list) {
            return list.stream().map(Object::toString).toList();
        }
        return List.of();
    }

    public List<String> getRoles(String token) {
        Object roles = parse(token).get("roles");

        if (roles instanceof List<?> list) {
            return list.stream().map(Object::toString).toList();
        }
        return List.of();
    }

    public String extractUsernameFromRefreshToken(String token) {
        return parse(token).getSubject();
    }

    public LocalDateTime extractExpiration(String token) {
        Date expiration = parse(token).getExpiration();

        return expiration.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    /**
     * Centraliza parsing (evita repetição)
     */
    private Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}