
package br.com.projeto.piloto.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.projeto.piloto.infrastructure.config.AuthProperties;
import io.jsonwebtoken.Jwts;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private AuthProperties authProperties;

    private static final String SECRET = "minha-chave-secreta-muito-longa-e-segura-12345";

    @BeforeEach
    void setUp() {
        authProperties = new AuthProperties();
        authProperties.getJwt().setSecret(SECRET);
        authProperties.getJwt().setExpirationMs(3_600_000L);        // 1 hora
        authProperties.getJwt().setRefreshExpirationMs(7_200_000L); // 2 horas
        jwtUtil = new JwtUtil(authProperties);
    }

    @Test
    @DisplayName("generateToken - retorna token não-nulo e válido")
    void generateToken_retornaTokenValido() {
        String token = jwtUtil.generateToken("usuario", Set.of("READ", "WRITE"));
        assertNotNull(token);
        assertTrue(jwtUtil.validate(token));
    }

    @Test
    @DisplayName("generateToken - subject extraível via getUsername")
    void generateToken_subjectExtravel() {
        String token = jwtUtil.generateToken("usuario.teste", Set.of("ADMIN"));
        assertEquals("usuario.teste", jwtUtil.getUsername(token));
    }

    @Test
    @DisplayName("generateToken - authorities extraíveis via getAuthorities")
    void generateToken_authoritiesExtravels() {
        Set<String> authorities = Set.of("READ", "CREATE", "UPDATE", "DELETE");
        String token = jwtUtil.generateToken("user", authorities);

        List<String> extracted = jwtUtil.getAuthorities(token);
        assertTrue(extracted.containsAll(authorities));
    }

    @Test
    @DisplayName("generateRefreshToken - retorna refresh token não-nulo e válido")
    void generateRefreshToken_retornaTokenValido() {
        String refresh = jwtUtil.generateRefreshToken("usuario.refresh");
        assertNotNull(refresh);
        assertTrue(jwtUtil.validate(refresh));
    }

    @Test
    @DisplayName("generateRefreshToken - subject extraível via extractUsernameFromRefreshToken")
    void generateRefreshToken_subjectExtravel() {
        String refresh = jwtUtil.generateRefreshToken("usuario.refresh");
        assertEquals("usuario.refresh", jwtUtil.extractUsernameFromRefreshToken(refresh));
    }

    @Test
    @DisplayName("validate - retorna false para token completamente inválido")
    void validate_tokenInvalido_retornaFalse() {
        assertFalse(jwtUtil.validate("token-totalmente-errado"));
    }

    @Test
    @DisplayName("validate - retorna false para token expirado")
    void validate_tokenExpirado_retornaFalse() {
        AuthProperties propsExpiradas = new AuthProperties();
        propsExpiradas.getJwt().setSecret(SECRET);
        propsExpiradas.getJwt().setExpirationMs(-1000L);
        propsExpiradas.getJwt().setRefreshExpirationMs(-1000L);

        JwtUtil jwtExpiradoUtil = new JwtUtil(propsExpiradas);
        String tokenVencido = jwtExpiradoUtil.generateToken("user", Set.of("ADMIN"));

        assertFalse(jwtUtil.validate(tokenVencido));
    }

    @Test
    @DisplayName("getAuthorities - claim 'authorities' é List → retorna lista mapeada")
    void getAuthorities_claimEhLista_retornaLista() throws Exception {
        Key key = extrairKey();
        Date now = new Date();
        Date exp = new Date(now.getTime() + authProperties.getJwt().getExpirationMs());

        String token = Jwts.builder()
                .setSubject("userAuthsList")
                .claim("authorities", List.of("A", "B"))
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key)
                .compact();

        List<String> auths = jwtUtil.getAuthorities(token);
        assertNotNull(auths);
        assertTrue(auths.contains("A"));
        assertTrue(auths.contains("B"));
    }

    @Test
    @DisplayName("getAuthorities - claim 'authorities' não é List → retorna lista vazia")
    void getAuthorities_claimNaoEhLista_retornaVazio() throws Exception {
        Key key = extrairKey();
        Date now = new Date();
        Date exp = new Date(now.getTime() + authProperties.getJwt().getExpirationMs());

        String token = Jwts.builder()
                .setSubject("userAuths")
                .claim("authorities", "not-a-list")
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key)
                .compact();

        List<String> auths = jwtUtil.getAuthorities(token);
        assertNotNull(auths);
        assertTrue(auths.isEmpty());
    }

    @Test
    @DisplayName("getAuthorities - claim 'authorities' ausente (refresh token) → retorna lista vazia")
    void getAuthorities_claimAusente_retornaVazio() {
        String refresh = jwtUtil.generateRefreshToken("usr3");

        List<String> auths = jwtUtil.getAuthorities(refresh);
        assertNotNull(auths);
        assertTrue(auths.isEmpty());
    }

    @Test
    @DisplayName("getRoles - claim 'roles' é List → retorna lista mapeada")
    void getRoles_claimEhLista_retornaLista() throws Exception {
        Key key = extrairKey();
        Date now = new Date();
        Date exp = new Date(now.getTime() + authProperties.getJwt().getExpirationMs());

        String token = Jwts.builder()
                .setSubject("userRoles")
                .claim("roles", List.of("ROLE_A", "ROLE_B"))
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key)
                .compact();

        List<String> roles = jwtUtil.getRoles(token);
        assertNotNull(roles);
        assertTrue(roles.contains("ROLE_A"));
        assertTrue(roles.contains("ROLE_B"));
    }

    @Test
    @DisplayName("getRoles - claim 'roles' ausente (refresh token) → retorna lista vazia")
    void getRoles_claimAusente_retornaVazio() {
        String refresh = jwtUtil.generateRefreshToken("userSemRoles");

        List<String> roles = jwtUtil.getRoles(refresh);
        assertNotNull(roles);
        assertTrue(roles.isEmpty());
    }

    @Test
    @DisplayName("getRoles - claim 'roles' não é List → retorna lista vazia")
    void getRoles_claimNaoEhLista_retornaVazio() throws Exception {
        Key key = extrairKey();
        Date now = new Date();
        Date exp = new Date(now.getTime() + authProperties.getJwt().getExpirationMs());

        String token = Jwts.builder()
                .setSubject("userRolesString")
                .claim("roles", "not-a-list")
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key)
                .compact();

        List<String> roles = jwtUtil.getRoles(token);
        assertNotNull(roles);
        assertTrue(roles.isEmpty());
    }

    @Test
    @DisplayName("extractExpiration - retorna LocalDateTime de expiração no futuro")
    void extractExpiration_retornaDataFutura() {
        String refresh = jwtUtil.generateRefreshToken("usr2");

        LocalDateTime expiration = jwtUtil.extractExpiration(refresh);

        assertNotNull(expiration);
        assertTrue(expiration.isAfter(LocalDateTime.now()));
    }

    @Test
    @DisplayName("extractExpiration - expiração de token de acesso está no futuro")
    void extractExpiration_tokenAcesso_retornaDataFutura() {
        String token = jwtUtil.generateToken("usr4", Set.of("READ"));

        LocalDateTime expiration = jwtUtil.extractExpiration(token);

        assertNotNull(expiration);
        assertTrue(expiration.isAfter(LocalDateTime.now()));
    }

    private Key extrairKey() throws Exception {
        Field keyField = JwtUtil.class.getDeclaredField("key");
        keyField.setAccessible(true);
        return (Key) keyField.get(jwtUtil);
    }
}
