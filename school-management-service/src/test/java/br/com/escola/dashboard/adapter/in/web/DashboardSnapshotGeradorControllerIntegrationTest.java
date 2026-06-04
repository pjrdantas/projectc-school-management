package br.com.escola.dashboard.adapter.in.web;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(
        statements = {
                "DELETE FROM dashboard_indicador_snapshot WHERE id_publico_dashboard IN (SELECT id_publico_dashboard FROM publico_dashboard WHERE codigo IN ('ACADEMICO', 'SECRETARIA', 'DIRETOR', 'PROFESSOR'))",
                "DELETE FROM publico_dashboard WHERE codigo IN ('ACADEMICO', 'SECRETARIA', 'DIRETOR', 'PROFESSOR')",
                "DELETE FROM professor WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'dashboard.snapshot.professor.%')",
                "DELETE FROM pessoa WHERE email LIKE 'dashboard.snapshot.professor.%'"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
        statements = {
                "DELETE FROM dashboard_indicador_snapshot WHERE id_publico_dashboard IN (SELECT id_publico_dashboard FROM publico_dashboard WHERE codigo IN ('ACADEMICO', 'SECRETARIA', 'DIRETOR', 'PROFESSOR'))",
                "DELETE FROM publico_dashboard WHERE codigo IN ('ACADEMICO', 'SECRETARIA', 'DIRETOR', 'PROFESSOR')",
                "DELETE FROM professor WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'dashboard.snapshot.professor.%')",
                "DELETE FROM pessoa WHERE email LIKE 'dashboard.snapshot.professor.%'"
        },
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class DashboardSnapshotGeradorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @WithMockUser
    void deveGerarSnapshotsAcademicosDeFormaIdempotente() throws Exception {
        UUID publicoId = criarPublico("ACADEMICO", "Dashboard acadêmico");

        mockMvc.perform(post("/api/dashboard/snapshots/geracoes/{publicoCodigo}", "academico")
                        .param("referenciaData", "2056-05-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$[?(@.codigoIndicador == 'TOTAL_MATRICULAS')].publicoCodigo")
                        .value(hasItem("ACADEMICO")));

        mockMvc.perform(post("/api/dashboard/snapshots/geracoes/{publicoCodigo}", "academico")
                        .param("referenciaData", "2056-05-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.codigoIndicador == 'TOTAL_MATRICULAS')].publicoCodigo")
                        .value(hasItem("ACADEMICO")));

        mockMvc.perform(get("/api/dashboard/snapshots")
                        .param("publicoDashboardId", publicoId.toString())
                        .param("referenciaData", "2056-05-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.codigoIndicador == 'TOTAL_MATRICULAS')].publicoCodigo")
                        .value(hasItem("ACADEMICO")));
    }

    @Test
    @WithMockUser
    void deveGerarSnapshotsDaSecretaria() throws Exception {
        criarPublico("SECRETARIA", "Dashboard secretaria");

        mockMvc.perform(post("/api/dashboard/snapshots/geracoes/{publicoCodigo}", "secretaria")
                        .param("referenciaData", "2056-05-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.codigoIndicador == 'MATRICULAS_SOLICITADAS')].publicoCodigo")
                        .value(hasItem("SECRETARIA")));
    }

    @Test
    @WithMockUser
    void deveRejeitarGeracaoParaPublicoNaoSuportado() throws Exception {
        criarPublico("PROFESSOR", "Dashboard professor");

        mockMvc.perform(post("/api/dashboard/snapshots/geracoes/{publicoCodigo}", "professor")
                        .param("referenciaData", "2056-05-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deveGerarSnapshotsDoProfessorPorDocente() throws Exception {
        UUID publicoId = criarPublico("PROFESSOR", "Dashboard professor");
        UUID professorId = criarProfessor();
        String codigoEsperado = "PROFESSOR_" + professorId.toString().replace("-", "").toUpperCase() + "_TURMAS_VINCULADAS";

        mockMvc.perform(post("/api/dashboard/snapshots/geracoes/professores/{professorId}", professorId)
                        .param("referenciaData", "2056-05-03"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$[?(@.codigoIndicador == '" + codigoEsperado + "')].publicoCodigo")
                        .value(hasItem("PROFESSOR")))
                .andExpect(jsonPath("$[?(@.codigoIndicador == '" + codigoEsperado + "')].valorTexto")
                        .value(hasItem(professorId.toString())));

        mockMvc.perform(post("/api/dashboard/snapshots/geracoes/professores/{professorId}", professorId)
                        .param("referenciaData", "2056-05-03"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.codigoIndicador == '" + codigoEsperado + "')].publicoCodigo")
                        .value(hasItem("PROFESSOR")));

        mockMvc.perform(get("/api/dashboard/snapshots")
                        .param("publicoDashboardId", publicoId.toString())
                        .param("referenciaData", "2056-05-03"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.codigoIndicador == '" + codigoEsperado + "')].valorTexto")
                        .value(hasItem(professorId.toString())));
    }

    private UUID criarPublico(String codigo, String descricao) {
        UUID publicoId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO publico_dashboard (id_publico_dashboard, codigo, descricao)
                VALUES (?, ?, ?)
                """, publicoId, codigo, descricao);
        return publicoId;
    }

    private UUID criarProfessor() {
        UUID pessoaId = UUID.randomUUID();
        UUID professorId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO pessoa (id_pessoa, nome_completo, cpf, email, ativo, created_at)
                VALUES (?, 'Professor Snapshot', ?, ?, true, CURRENT_TIMESTAMP)
                """, pessoaId, cpfAleatorio(), "dashboard.snapshot.professor." + System.nanoTime() + "@example.com");
        jdbcTemplate.update("""
                INSERT INTO professor (id_professor, id_pessoa, registro_profissional, formacao, ativo, created_at)
                VALUES (?, ?, ?, 'Licenciatura', true, CURRENT_TIMESTAMP)
                """, professorId, pessoaId, "RP-SNAP-" + System.nanoTime());
        return professorId;
    }

    private String cpfAleatorio() {
        long cpf = System.nanoTime() % 1_000_000_00000L;
        return String.format("%011d", cpf);
    }
}
