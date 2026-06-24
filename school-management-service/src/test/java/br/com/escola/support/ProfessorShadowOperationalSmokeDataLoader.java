package br.com.escola.support;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("professor-shadow-operational")
public class ProfessorShadowOperationalSmokeDataLoader implements ApplicationRunner {

    public static final UUID ESCOLA_ID = UUID.fromString("00000000-0000-0000-0000-000000000047");
    public static final UUID USUARIO_ID = UUID.fromString("30000000-0000-0000-0000-000000000001");
    public static final UUID PESSOA_ID = UUID.fromString("30000000-0000-0000-0000-000000000002");
    public static final UUID CARGO_ID = UUID.fromString("30000000-0000-0000-0000-000000000003");
    public static final UUID FUNCIONARIO_ID = UUID.fromString("30000000-0000-0000-0000-000000000004");
    public static final UUID PROFESSOR_ID = UUID.fromString("30000000-0000-0000-0000-000000000005");
    public static final String USERNAME = "professor-shadow-smoke";
    public static final String PASSWORD = "senha123";

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public ProfessorShadowOperationalSmokeDataLoader(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        inserirUsuarioSeAusente();
        inserirProfessorSeAusente();
    }

    private void inserirUsuarioSeAusente() {
        Integer total = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM usuario WHERE id_usuario = ?",
                Integer.class,
                USUARIO_ID);
        if (total != null && total > 0) {
            return;
        }

        jdbcTemplate.update("""
                INSERT INTO usuario (id_usuario, username, nome, email, senha_hash, ativo, created_at)
                VALUES (?, ?, ?, ?, ?, true, ?)
                """,
                USUARIO_ID,
                USERNAME,
                "Professor Shadow Smoke",
                "professor.shadow.smoke@example.com",
                passwordEncoder.encode(PASSWORD),
                LocalDateTime.now());
    }

    private void inserirProfessorSeAusente() {
        Integer totalProfessor = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM professor WHERE id_professor = ?",
                Integer.class,
                PROFESSOR_ID);
        if (totalProfessor != null && totalProfessor > 0) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        if (!registroExiste("SELECT COUNT(1) FROM escola WHERE id_escola = ?", ESCOLA_ID)) {
            jdbcTemplate.update("""
                    INSERT INTO escola (id_escola, nome, ativo, created_at)
                    VALUES (?, ?, true, ?)
                    """,
                    ESCOLA_ID,
                    "Escola padrão",
                    now);
        }

        if (!registroExiste("SELECT COUNT(1) FROM pessoa WHERE id_pessoa = ?", PESSOA_ID)) {
            jdbcTemplate.update("""
                    INSERT INTO pessoa (id_pessoa, nome_completo, cpf, email, id_escola, ativo, created_at)
                    VALUES (?, ?, ?, ?, ?, true, ?)
                    """,
                    PESSOA_ID,
                    "Professor Shadow Smoke",
                    "30000000001",
                    "professor.shadow.smoke@example.com",
                    ESCOLA_ID,
                    now);
        }

        if (!registroExiste("SELECT COUNT(1) FROM cargo WHERE id_cargo = ?", CARGO_ID)) {
            jdbcTemplate.update("""
                    INSERT INTO cargo (id_cargo, codigo, descricao)
                    VALUES (?, ?, ?)
                    """,
                    CARGO_ID,
                    "PROF-SHADOW-SMOKE",
                    "Professor");
        }

        if (!registroExiste("SELECT COUNT(1) FROM funcionario WHERE id_funcionario = ?", FUNCIONARIO_ID)) {
            jdbcTemplate.update("""
                    INSERT INTO funcionario (id_funcionario, id_pessoa, id_cargo, ativo, created_at)
                    VALUES (?, ?, ?, true, ?)
                    """,
                    FUNCIONARIO_ID,
                    PESSOA_ID,
                    CARGO_ID,
                    now);
        }

        jdbcTemplate.update("""
                INSERT INTO professor
                (id_professor, registro_profissional, formacao, ativo, created_at, updated_at, id_pessoa, id_usuario)
                VALUES (?, ?, ?, true, ?, ?, ?, ?)
                """,
                PROFESSOR_ID,
                "RP-SHADOW-SMOKE",
                "Licenciatura em Matemática",
                now,
                now,
                PESSOA_ID,
                USUARIO_ID);
    }

    private boolean registroExiste(String sql, UUID id) {
        Integer total = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return total != null && total > 0;
    }
}
