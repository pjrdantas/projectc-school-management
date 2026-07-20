package br.com.escola.identityaccessservice.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class SessaoInternaControllerIntegrationTest {

    private static final UUID ESCOLA_PADRAO_ID = UUID.fromString("00000000-0000-0000-0000-000000000047");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("identity-access.internal-api.token", () -> "identity-token");
        registry.add("identity-access.session-cleanup.enabled", () -> false);
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:identityaccess;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
    }

    @BeforeEach
    void prepararBanco() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS sessao_autenticacao");
        jdbcTemplate.execute("DROP TABLE IF EXISTS usuario_escola");
        jdbcTemplate.execute("DROP TABLE IF EXISTS perfil_permissao");
        jdbcTemplate.execute("DROP TABLE IF EXISTS usuario_perfil");
        jdbcTemplate.execute("DROP TABLE IF EXISTS permissao");
        jdbcTemplate.execute("DROP TABLE IF EXISTS perfil");
        jdbcTemplate.execute("DROP TABLE IF EXISTS usuario");
        jdbcTemplate.execute("DROP TABLE IF EXISTS escola");

        jdbcTemplate.execute("""
                CREATE TABLE escola (
                    id_escola UUID PRIMARY KEY,
                    nome VARCHAR(150) NOT NULL,
                    ativo BOOLEAN NOT NULL,
                    created_at TIMESTAMP NOT NULL
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE usuario (
                    id_usuario UUID PRIMARY KEY,
                    username VARCHAR(80) NOT NULL UNIQUE,
                    nome VARCHAR(150) NOT NULL,
                    email VARCHAR(150) NOT NULL UNIQUE,
                    senha_hash VARCHAR(255) NOT NULL,
                    ativo BOOLEAN NOT NULL,
                    created_at TIMESTAMP NOT NULL,
                    id_escola UUID NULL
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE sessao_autenticacao (
                    id_sessao_autenticacao UUID PRIMARY KEY,
                    id_usuario UUID NOT NULL,
                    id_escola UUID NULL,
                    refresh_token_hash VARCHAR(255) NOT NULL,
                    access_token_hash VARCHAR(255),
                    expira_em TIMESTAMP NOT NULL,
                    access_expira_em TIMESTAMP,
                    revogado BOOLEAN NOT NULL,
                    created_at TIMESTAMP NOT NULL
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE usuario_escola (
                    id_usuario_escola UUID PRIMARY KEY,
                    id_usuario UUID NOT NULL,
                    id_escola UUID NOT NULL,
                    created_at TIMESTAMP NOT NULL,
                    UNIQUE (id_usuario, id_escola)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE perfil (
                    id_perfil UUID PRIMARY KEY,
                    codigo VARCHAR(80) NOT NULL UNIQUE,
                    nome VARCHAR(120) NOT NULL,
                    descricao VARCHAR(255),
                    created_at TIMESTAMP NOT NULL
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE permissao (
                    id_permissao UUID PRIMARY KEY,
                    codigo VARCHAR(120) NOT NULL UNIQUE,
                    descricao VARCHAR(255),
                    created_at TIMESTAMP NOT NULL
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE usuario_perfil (
                    id_usuario_perfil UUID PRIMARY KEY,
                    id_usuario UUID NOT NULL,
                    id_perfil UUID NOT NULL
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE perfil_permissao (
                    id_perfil_permissao UUID PRIMARY KEY,
                    id_perfil UUID NOT NULL,
                    id_permissao UUID NOT NULL
                )
                """);
    }

    @Test
    void deveAdministrarCicloCompletoDePermissao() throws Exception {
        String response = mockMvc.perform(post("/internal/v1/permissoes")
                        .contentType("application/json")
                        .content("{\"nmPermissao\":\" matricula_editar \",\"descricao\":\" Editar matricula \"}")
                        .headers(internalHeaders("corr-permissao-create")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("MATRICULA_EDITAR"))
                .andExpect(jsonPath("$.nmPermissao").value("MATRICULA_EDITAR"))
                .andExpect(jsonPath("$.descricao").value("Editar matricula"))
                .andReturn().getResponse().getContentAsString();
        String permissaoId = JsonPath.read(response, "$.id");

        mockMvc.perform(get("/internal/v1/permissoes")
                        .headers(internalHeaders("corr-permissao-list")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(permissaoId));

        mockMvc.perform(get("/internal/v1/permissoes/{id}", permissaoId)
                        .headers(internalHeaders("corr-permissao-get")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("MATRICULA_EDITAR"));

        mockMvc.perform(put("/internal/v1/permissoes/{id}", permissaoId)
                        .contentType("application/json")
                        .content("{\"codigo\":\"MATRICULA_GERENCIAR\",\"descricao\":\"Gerenciar matricula\"}")
                        .headers(internalHeaders("corr-permissao-update")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("MATRICULA_GERENCIAR"));

        UUID perfilId = UUID.randomUUID();
        inserirPerfil(perfilId, "SECRETARIA", "Secretaria");
        jdbcTemplate.update("""
                INSERT INTO perfil_permissao (id_perfil_permissao, id_perfil, id_permissao)
                VALUES (?, ?, ?)
                """, UUID.randomUUID(), perfilId, UUID.fromString(permissaoId));

        mockMvc.perform(delete("/internal/v1/permissoes/{id}", permissaoId)
                        .headers(internalHeaders("corr-permissao-conflict")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));

        jdbcTemplate.update("DELETE FROM perfil_permissao WHERE id_permissao = ?", UUID.fromString(permissaoId));
        mockMvc.perform(delete("/internal/v1/permissoes/{id}", permissaoId)
                        .headers(internalHeaders("corr-permissao-delete")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/internal/v1/permissoes/{id}", permissaoId)
                        .headers(internalHeaders("corr-permissao-missing")))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAdministrarCicloCompletoDePerfilESeusVinculos() throws Exception {
        UUID permissaoInicialId = UUID.randomUUID();
        UUID permissaoNovaId = UUID.randomUUID();
        inserirPermissao(permissaoInicialId, "ALUNO_LER");
        inserirPermissao(permissaoNovaId, "ALUNO_EDITAR");

        String response = mockMvc.perform(post("/internal/v1/perfis")
                        .contentType("application/json")
                        .content("""
                                {
                                  "codigo":" secretaria ",
                                  "nmPerfil":" Secretaria Escolar ",
                                  "descricao":" Operacao da secretaria ",
                                  "permissoesIds":["%s"]
                                }
                                """.formatted(permissaoInicialId))
                        .headers(internalHeaders("corr-perfil-create")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("SECRETARIA"))
                .andExpect(jsonPath("$.nome").value("Secretaria Escolar"))
                .andExpect(jsonPath("$.nmPerfil").value("Secretaria Escolar"))
                .andExpect(jsonPath("$.permissaoIds[0]").value(permissaoInicialId.toString()))
                .andReturn().getResponse().getContentAsString();
        String perfilId = JsonPath.read(response, "$.id");

        mockMvc.perform(get("/internal/v1/perfis")
                        .headers(internalHeaders("corr-perfil-list")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(perfilId));

        mockMvc.perform(put("/internal/v1/perfis/{id}", perfilId)
                        .contentType("application/json")
                        .content("""
                                {
                                  "codigo":"GESTAO_SECRETARIA",
                                  "nome":"Gestao da Secretaria",
                                  "permissaoIds":["%s"]
                                }
                                """.formatted(permissaoNovaId))
                        .headers(internalHeaders("corr-perfil-update")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("GESTAO_SECRETARIA"))
                .andExpect(jsonPath("$.permissaoIds[0]").value(permissaoNovaId.toString()));

        Integer oldLinks = jdbcTemplate.queryForObject("""
                SELECT COUNT(1) FROM perfil_permissao
                WHERE id_perfil = ? AND id_permissao = ?
                """, Integer.class, UUID.fromString(perfilId), permissaoInicialId);
        assertThat(oldLinks).isZero();

        UUID usuarioId = UUID.randomUUID();
        inserirUsuario(usuarioId, "usuario.perfil", null);
        jdbcTemplate.update("""
                INSERT INTO usuario_perfil (id_usuario_perfil, id_usuario, id_perfil)
                VALUES (?, ?, ?)
                """, UUID.randomUUID(), usuarioId, UUID.fromString(perfilId));

        mockMvc.perform(delete("/internal/v1/perfis/{id}", perfilId)
                        .headers(internalHeaders("corr-perfil-conflict")))
                .andExpect(status().isConflict());

        jdbcTemplate.update("DELETE FROM usuario_perfil WHERE id_perfil = ?", UUID.fromString(perfilId));
        mockMvc.perform(delete("/internal/v1/perfis/{id}", perfilId)
                        .headers(internalHeaders("corr-perfil-delete")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/internal/v1/perfis/{id}", perfilId)
                        .headers(internalHeaders("corr-perfil-missing")))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAdministrarCicloCompletoDeUsuarioESeusPerfis() throws Exception {
        UUID perfilInicialId = UUID.randomUUID();
        UUID perfilNovoId = UUID.randomUUID();
        inserirPerfil(perfilInicialId, "SECRETARIA", "Secretaria");
        inserirPerfil(perfilNovoId, "DIRECAO", "Direcao");

        String response = mockMvc.perform(post("/internal/v1/usuarios")
                        .contentType("application/json")
                        .content("""
                                {
                                  "login":" usuario.admin ",
                                  "nome":" Usuario Administrativo ",
                                  "email":"USUARIO@ESCOLA.COM",
                                  "senha":"senha-segura",
                                  "ativo":true,
                                  "perfisIds":["%s"]
                                }
                                """.formatted(perfilInicialId))
                        .headers(internalHeaders("corr-usuario-create")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("usuario.admin"))
                .andExpect(jsonPath("$.login").value("usuario.admin"))
                .andExpect(jsonPath("$.email").value("usuario@escola.com"))
                .andExpect(jsonPath("$.perfilIds[0]").value(perfilInicialId.toString()))
                .andExpect(jsonPath("$.senhaHash").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        String usuarioId = JsonPath.read(response, "$.id");

        String senhaPersistida = jdbcTemplate.queryForObject(
                "SELECT senha_hash FROM usuario WHERE id_usuario = ?",
                String.class,
                UUID.fromString(usuarioId));
        assertThat(senhaPersistida).isNotEqualTo("senha-segura");
        assertThat(passwordEncoder.matches("senha-segura", senhaPersistida)).isTrue();

        mockMvc.perform(get("/internal/v1/usuarios")
                        .headers(internalHeaders("corr-usuario-list")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(usuarioId));

        mockMvc.perform(put("/internal/v1/usuarios/{id}", usuarioId)
                        .contentType("application/json")
                        .content("""
                                {
                                  "username":"usuario.gestor",
                                  "nome":"Usuario Gestor",
                                  "email":"gestor@escola.com",
                                  "senhaHash":"nova-senha",
                                  "ativo":true,
                                  "perfilIds":["%s"]
                                }
                                """.formatted(perfilNovoId))
                        .headers(internalHeaders("corr-usuario-update")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("usuario.gestor"))
                .andExpect(jsonPath("$.perfilIds[0]").value(perfilNovoId.toString()));

        Integer oldLinks = jdbcTemplate.queryForObject("""
                SELECT COUNT(1) FROM usuario_perfil
                WHERE id_usuario = ? AND id_perfil = ?
                """, Integer.class, UUID.fromString(usuarioId), perfilInicialId);
        assertThat(oldLinks).isZero();

        mockMvc.perform(post("/internal/v1/usuarios")
                        .contentType("application/json")
                        .content("""
                                {
                                  "username":"outro.usuario",
                                  "nome":"Outro Usuario",
                                  "email":"gestor@escola.com",
                                  "senhaHash":"senha",
                                  "perfilIds":["%s"]
                                }
                                """.formatted(perfilNovoId))
                        .headers(internalHeaders("corr-usuario-conflict")))
                .andExpect(status().isConflict());

        inserirSessao(UUID.fromString(usuarioId), null, "token-usuario-administrado");
        mockMvc.perform(delete("/internal/v1/usuarios/{id}", usuarioId)
                        .headers(internalHeaders("corr-usuario-delete")))
                .andExpect(status().isNoContent());

        Integer sessions = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM sessao_autenticacao WHERE id_usuario = ?",
                Integer.class,
                UUID.fromString(usuarioId));
        assertThat(sessions).isZero();

        mockMvc.perform(get("/internal/v1/usuarios/{id}", usuarioId)
                        .headers(internalHeaders("corr-usuario-missing")))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAutenticarRotacionarEEncerrarSessao() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        UUID perfilId = UUID.randomUUID();
        UUID permissaoId = UUID.randomUUID();
        inserirUsuario(usuarioId, "usuario.login", null);
        jdbcTemplate.update(
                "UPDATE usuario SET senha_hash = ? WHERE id_usuario = ?",
                passwordEncoder.encode("senha-segura"), usuarioId);
        inserirPerfil(perfilId, "SECRETARIA", "Secretaria");
        jdbcTemplate.update(
                "INSERT INTO permissao (id_permissao, codigo, created_at) VALUES (?, ?, ?)",
                permissaoId, "MATRICULA_EDITAR", Timestamp.valueOf(LocalDateTime.now()));
        jdbcTemplate.update(
                "INSERT INTO usuario_perfil (id_usuario_perfil, id_usuario, id_perfil) VALUES (?, ?, ?)",
                UUID.randomUUID(), usuarioId, perfilId);
        jdbcTemplate.update("""
                INSERT INTO perfil_permissao (
                    id_perfil_permissao, id_perfil, id_permissao
                ) VALUES (?, ?, ?)
                """, UUID.randomUUID(), perfilId, permissaoId);

        String loginResponse = mockMvc.perform(post("/internal/v1/auth/login")
                        .contentType("application/json")
                        .content("""
                                {"login":"usuario.login","senha":"senha-segura"}
                                """)
                        .header("X-Internal-Token", "identity-token")
                        .header("X-Correlation-Id", "corr-auth-login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.usuarioId").value(usuarioId.toString()))
                .andExpect(jsonPath("$.perfis[0]").value("SECRETARIA"))
                .andExpect(jsonPath("$.permissoes[0]").value("MATRICULA_EDITAR"))
                .andReturn().getResponse().getContentAsString();
        String refreshToken = JsonPath.read(loginResponse, "$.refreshToken");

        String refreshResponse = mockMvc.perform(post("/internal/v1/auth/refresh")
                        .contentType("application/json")
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}")
                        .header("X-Internal-Token", "identity-token")
                        .header("X-Correlation-Id", "corr-auth-refresh"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String rotatedRefreshToken = JsonPath.read(refreshResponse, "$.refreshToken");
        assertThat(rotatedRefreshToken).isNotEqualTo(refreshToken);

        mockMvc.perform(post("/internal/v1/auth/refresh")
                        .contentType("application/json")
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}")
                        .header("X-Internal-Token", "identity-token")
                        .header("X-Correlation-Id", "corr-auth-old-refresh"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/internal/v1/auth/logout")
                        .contentType("application/json")
                        .content("{\"refreshToken\":\"" + rotatedRefreshToken + "\"}")
                        .header("X-Internal-Token", "identity-token")
                        .header("X-Correlation-Id", "corr-auth-logout"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deveListarEscolasDaSessaoNoContratoInterno() throws Exception {
        UUID escolaAtivaId = UUID.randomUUID();
        UUID outraEscolaId = UUID.randomUUID();

        UUID usuarioId = UUID.randomUUID();
        String token = "identity-user-token";
        inserirEscola(escolaAtivaId, "Escola Ativa");
        inserirEscola(outraEscolaId, "Escola Opcional");
        inserirUsuario(usuarioId, "usuario.identity", escolaAtivaId);
        inserirVinculo(usuarioId, escolaAtivaId);
        inserirVinculo(usuarioId, outraEscolaId);
        inserirSessao(usuarioId, escolaAtivaId, token);

        mockMvc.perform(get("/internal/v1/auth/escolas")
                        .header("X-Internal-Token", "identity-token")
                        .header("X-Correlation-Id", "corr-identity-1")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].escolaId").value(escolaAtivaId.toString()))
                .andExpect(jsonPath("$[0].ativa").value(true))
                .andExpect(jsonPath("$[1].escolaNome").value("Escola Opcional"));

    }

    @Test
    void deveConsultarContextoAtualNoContratoInterno() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        String token = "identity-user-token";

        inserirEscola(escolaId, "Escola Contexto");
        inserirUsuario(usuarioId, "usuario.contexto", escolaId);
        inserirSessao(usuarioId, null, token);

        mockMvc.perform(get("/internal/v1/auth/contexto-atual")
                        .header("X-Internal-Token", "identity-token")
                        .header("X-Correlation-Id", "corr-identity-ctx")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value(usuarioId.toString()))
                .andExpect(jsonPath("$.escolaId").value(escolaId.toString()))
                .andExpect(jsonPath("$.escolaNome").value("Escola Contexto"))
                .andExpect(jsonPath("$.username").value("usuario.contexto"));

    }

    @Test
    void deveAplicarEscolaPadraoQuandoSessaoEUsuarioNaoPossuemEscola() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        String token = "identity-user-token-default";

        inserirUsuario(usuarioId, "usuario.default", null);
        inserirSessao(usuarioId, null, token);

        mockMvc.perform(get("/internal/v1/auth/contexto-atual")
                        .header("X-Internal-Token", "identity-token")
                        .header("X-Correlation-Id", "corr-identity-ctx-default")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value(usuarioId.toString()))
                .andExpect(jsonPath("$.escolaId").value(ESCOLA_PADRAO_ID.toString()))
                .andExpect(jsonPath("$.escolaNome").value("Escola padrao"))
                .andExpect(jsonPath("$.username").value("usuario.default"));

    }

    @Test
    void deveSelecionarEscolaAtivaNoContratoInterno() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();

        UUID escolaAnteriorId = UUID.randomUUID();
        String token = "identity-user-token";
        inserirEscola(escolaAnteriorId, "Escola Anterior");
        inserirEscola(escolaId, "Escola Selecionada");
        inserirUsuario(usuarioId, "usuario.identity", escolaAnteriorId);
        inserirVinculo(usuarioId, escolaAnteriorId);
        inserirVinculo(usuarioId, escolaId);
        inserirSessao(usuarioId, escolaAnteriorId, token);

        mockMvc.perform(post("/internal/v1/auth/escola-ativa")
                        .contentType("application/json")
                        .content("""
                                {
                                  "escolaId":"%s"
                                }
                                """.formatted(escolaId))
                        .header("X-Internal-Token", "identity-token")
                        .header("X-Correlation-Id", "corr-identity-2")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value(usuarioId.toString()))
                .andExpect(jsonPath("$.escolaId").value(escolaId.toString()))
                .andExpect(jsonPath("$.username").value("usuario.identity"));

        UUID escolaPersistida = jdbcTemplate.queryForObject(
                "SELECT id_escola FROM sessao_autenticacao WHERE access_token_hash = ?",
                UUID.class,
                hashToken(token));
        assertThat(escolaPersistida).isEqualTo(escolaId);
    }

    @Test
    void deveExigirTokenInternoValido() throws Exception {
        mockMvc.perform(get("/internal/v1/auth/escolas")
                        .header("X-Correlation-Id", "corr-identity-3")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", "Bearer identity-user-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INTERNAL_UNAUTHORIZED"));
    }

    private void inserirEscola(UUID escolaId, String nome) {
        jdbcTemplate.update("""
                INSERT INTO escola (id_escola, nome, ativo, created_at)
                VALUES (?, ?, ?, ?)
                """, escolaId, nome, true, Timestamp.valueOf(LocalDateTime.now()));
    }

    private org.springframework.http.HttpHeaders internalHeaders(String correlationId) {
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("X-Internal-Token", "identity-token");
        headers.add("X-Correlation-Id", correlationId);
        headers.add("X-Usuario-Id", UUID.randomUUID().toString());
        headers.add("X-Escola-Id", UUID.randomUUID().toString());
        return headers;
    }

    private void inserirUsuario(UUID usuarioId, String username, UUID escolaId) {
        jdbcTemplate.update("""
                INSERT INTO usuario (id_usuario, username, nome, email, senha_hash, ativo, created_at, id_escola)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                usuarioId,
                username,
                "Nome " + username,
                username + "@escola.com",
                "hash",
                true,
                Timestamp.valueOf(LocalDateTime.now()),
                escolaId);
    }

    private void inserirPerfil(UUID perfilId, String codigo, String nome) {
        jdbcTemplate.update("""
                INSERT INTO perfil (id_perfil, codigo, nome, created_at)
                VALUES (?, ?, ?, ?)
                """, perfilId, codigo, nome, Timestamp.valueOf(LocalDateTime.now()));
    }

    private void inserirPermissao(UUID permissaoId, String codigo) {
        jdbcTemplate.update("""
                INSERT INTO permissao (id_permissao, codigo, created_at)
                VALUES (?, ?, ?)
                """, permissaoId, codigo, Timestamp.valueOf(LocalDateTime.now()));
    }

    private void inserirVinculo(UUID usuarioId, UUID escolaId) {
        jdbcTemplate.update("""
                INSERT INTO usuario_escola (id_usuario_escola, id_usuario, id_escola, created_at)
                VALUES (?, ?, ?, ?)
                """, UUID.randomUUID(), usuarioId, escolaId, Timestamp.valueOf(LocalDateTime.now()));
    }

    private void inserirSessao(UUID usuarioId, UUID escolaId, String token) {
        jdbcTemplate.update("""
                INSERT INTO sessao_autenticacao (
                    id_sessao_autenticacao,
                    id_usuario,
                    id_escola,
                    refresh_token_hash,
                    access_token_hash,
                    expira_em,
                    access_expira_em,
                    revogado,
                    created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(),
                usuarioId,
                escolaId,
                "refresh",
                hashToken(token),
                Timestamp.valueOf(LocalDateTime.now().plusDays(1)),
                Timestamp.valueOf(LocalDateTime.now().plusMinutes(30)),
                false,
                Timestamp.valueOf(LocalDateTime.now()));
    }

    private String hashToken(String token) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}

