package br.com.escola.rh.adapter.in.web.internal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
                "DELETE FROM professor",
                "DELETE FROM funcionario WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'funcionario.professor.internal.%')",
                "DELETE FROM cargo WHERE codigo LIKE 'FUNC-PROF-INT-%'",
                "DELETE FROM pessoa WHERE email LIKE 'funcionario.professor.internal.%'"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
        statements = {
                "DELETE FROM professor",
                "DELETE FROM funcionario WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'funcionario.professor.internal.%')",
                "DELETE FROM cargo WHERE codigo LIKE 'FUNC-PROF-INT-%'",
                "DELETE FROM pessoa WHERE email LIKE 'funcionario.professor.internal.%'"
        },
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class FuncionarioProfessorInternalControllerIntegrationTest {

    private static final String ESCOLA_ID = "00000000-0000-0000-0000-000000000047";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @WithMockUser
    void deveConsultarFuncionarioEListarElegiveisNoAdaptadorInterno() throws Exception {
        UUID elegivelId = criarFuncionario(
                "Funcionario Professor Elegivel",
                "funcionario.professor.internal.elegivel@example.com",
                true);
        UUID naoElegivelId = criarFuncionario(
                "Funcionario Professor Inativo",
                "funcionario.professor.internal.inativo@example.com",
                false);

        mockMvc.perform(get("/internal/funcionarios/{id}/professor", elegivelId)
                        .header("X-Escola-Id", ESCOLA_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.funcionarioId").value(elegivelId.toString()))
                .andExpect(jsonPath("$.nomeCompleto").value("Funcionario Professor Elegivel"))
                .andExpect(jsonPath("$.ativo").value(true))
                .andExpect(jsonPath("$.elegivelProfessor").value(true));

        mockMvc.perform(get("/internal/funcionarios/{id}/professor", naoElegivelId)
                        .header("X-Escola-Id", ESCOLA_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.funcionarioId").value(naoElegivelId.toString()))
                .andExpect(jsonPath("$.ativo").value(false))
                .andExpect(jsonPath("$.elegivelProfessor").value(false));

        mockMvc.perform(get("/internal/funcionarios/professor-elegiveis")
                        .header("X-Escola-Id", ESCOLA_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].funcionarioId").value(elegivelId.toString()))
                .andExpect(jsonPath("$[0].elegivelProfessor").value(true))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void deveExigirAutenticacaoNoAdaptadorInterno() throws Exception {
        mockMvc.perform(get("/internal/funcionarios/professor-elegiveis")
                        .header("X-Escola-Id", ESCOLA_ID))
                .andExpect(status().isUnauthorized());
    }

    private UUID criarFuncionario(String nome, String email, boolean ativo) {
        UUID pessoaId = UUID.randomUUID();
        UUID cargoId = UUID.randomUUID();
        UUID funcionarioId = UUID.randomUUID();

        jdbcTemplate.update("""
                INSERT INTO pessoa (id_pessoa, nome_completo, cpf, email, id_escola, ativo, created_at)
                VALUES (?, ?, ?, ?, '00000000-0000-0000-0000-000000000047', ?, CURRENT_TIMESTAMP)
                """, pessoaId, nome, cpfAleatorio(), email, ativo);

        jdbcTemplate.update("""
                INSERT INTO cargo (id_cargo, codigo, descricao)
                VALUES (?, ?, ?)
                """, cargoId, "FUNC-PROF-INT-" + System.nanoTime(), "Professor");

        jdbcTemplate.update("""
                INSERT INTO funcionario (id_funcionario, id_pessoa, id_cargo, ativo, created_at)
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
                """, funcionarioId, pessoaId, cargoId, ativo);

        return funcionarioId;
    }

    private String cpfAleatorio() {
        long cpf = System.nanoTime() % 1_000_000_00000L;
        return String.format("%011d", cpf);
    }
}
