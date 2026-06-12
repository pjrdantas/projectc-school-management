package br.com.escola.seguranca.adapter.in.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(
        statements = {
                "DELETE FROM sessao_autenticacao WHERE id_usuario IN (SELECT id_usuario FROM usuario WHERE username LIKE 'professor44f%')",
                "DELETE FROM usuario_perfil WHERE id_usuario IN (SELECT id_usuario FROM usuario WHERE username LIKE 'professor44f%')",
                "DELETE FROM professor WHERE id_usuario IN (SELECT id_usuario FROM usuario WHERE username LIKE 'professor44f%')",
                "DELETE FROM usuario WHERE username LIKE 'professor44f%'",
                "DELETE FROM pessoa WHERE email LIKE 'professor44f.%'"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
        statements = {
                "DELETE FROM sessao_autenticacao WHERE id_usuario IN (SELECT id_usuario FROM usuario WHERE username LIKE 'professor44f%')",
                "DELETE FROM usuario_perfil WHERE id_usuario IN (SELECT id_usuario FROM usuario WHERE username LIKE 'professor44f%')",
                "DELETE FROM professor WHERE id_usuario IN (SELECT id_usuario FROM usuario WHERE username LIKE 'professor44f%')",
                "DELETE FROM usuario WHERE username LIKE 'professor44f%'",
                "DELETE FROM pessoa WHERE email LIKE 'professor44f.%'"
        },
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void deveRetornarProfessorIdQuandoUsuarioEstiverVinculadoAoProfessor() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        UUID pessoaId = UUID.randomUUID();
        UUID professorId = UUID.randomUUID();

        jdbcTemplate.update("""
                INSERT INTO usuario (id_usuario, username, nome, email, senha_hash, ativo, created_at)
                VALUES (?, 'professor44f', 'Professor 44F', 'professor44f.usuario@example.com', ?, true, CURRENT_TIMESTAMP)
                """, usuarioId, passwordEncoder.encode("senha123"));
        jdbcTemplate.update("""
                INSERT INTO pessoa (id_pessoa, nome_completo, cpf, email, ativo, created_at)
                VALUES (?, 'Professor 44F', ?, 'professor44f.usuario@example.com', true, CURRENT_TIMESTAMP)
                """, pessoaId, cpfAleatorio());
        jdbcTemplate.update("""
                INSERT INTO professor (id_professor, id_pessoa, id_usuario, registro_profissional, formacao, ativo, created_at)
                VALUES (?, ?, ?, 'RP-44F', 'Licenciatura', true, CURRENT_TIMESTAMP)
                """, professorId, pessoaId, usuarioId);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "login": "professor44f",
                                  "senha": "senha123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value(usuarioId.toString()))
                .andExpect(jsonPath("$.professorId").value(professorId.toString()))
                .andExpect(jsonPath("$.escolaId").value("00000000-0000-0000-0000-000000000047"))
                .andExpect(jsonPath("$.escolaNome").value("Escola padrão"))
                .andExpect(jsonPath("$.username").value("professor44f"));
    }

    private String cpfAleatorio() {
        long cpf = System.nanoTime() % 1_000_000_00000L;
        return String.format("%011d", cpf);
    }
}
