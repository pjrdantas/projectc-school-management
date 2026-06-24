package br.com.escola.professor.adapter.in.web.internal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(
        statements = {
                "DELETE FROM professor_turma_disciplina",
                "DELETE FROM professor",
                "DELETE FROM funcionario WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'professor.internal.controller.%')",
                "DELETE FROM cargo WHERE codigo LIKE 'PROF-INT-CTRL-%'",
                "DELETE FROM pessoa WHERE email LIKE 'professor.internal.controller.%'",
                "DELETE FROM turma_disciplina WHERE id_turma IN (SELECT id_turma FROM turma WHERE codigo LIKE 'PROF-IC-%')",
                "DELETE FROM disciplina WHERE nome LIKE 'Professor Internal Controller %'",
                "DELETE FROM turma WHERE codigo LIKE 'PROF-IC-%'",
                "DELETE FROM periodo_letivo WHERE nome LIKE 'PROF-IC-%'"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
        statements = {
                "DELETE FROM professor_turma_disciplina",
                "DELETE FROM professor",
                "DELETE FROM funcionario WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'professor.internal.controller.%')",
                "DELETE FROM cargo WHERE codigo LIKE 'PROF-INT-CTRL-%'",
                "DELETE FROM pessoa WHERE email LIKE 'professor.internal.controller.%'",
                "DELETE FROM turma_disciplina WHERE id_turma IN (SELECT id_turma FROM turma WHERE codigo LIKE 'PROF-IC-%')",
                "DELETE FROM disciplina WHERE nome LIKE 'Professor Internal Controller %'",
                "DELETE FROM turma WHERE codigo LIKE 'PROF-IC-%'",
                "DELETE FROM periodo_letivo WHERE nome LIKE 'PROF-IC-%'"
        },
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class ProfessorInternalControllerIntegrationTest {

    private static final String ESCOLA_ID = "00000000-0000-0000-0000-000000000047";
    private static final UUID SERIE_PADRAO_ID = UUID.fromString("00000000-0000-0000-0000-000000000100");
    private static final UUID TURNO_MANHA_ID = UUID.fromString("00000000-0000-0000-0000-000000000051");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @WithMockUser
    void deveCriarBuscarEAlocarProfessorPeloAdaptadorInterno() throws Exception {
        UUID funcionarioId = criarFuncionario("Professor Internal Controller Fluxo", "professor.internal.controller.fluxo@example.com");
        UUID periodoId = criarPeriodo("PROF-IC-2039.1", "2039-02-01", "2039-06-30");
        UUID turmaId = criarTurma("PROF-IC-A", "Professor Internal Controller Turma A", 30, periodoId);
        UUID disciplinaId = criarDisciplina("Professor Internal Controller Matematica", 80);
        UUID turmaDisciplinaId = vincularDisciplina(turmaId, disciplinaId, 80);

        String professorRequest = """
                {
                  "funcionarioId": "%s",
                  "registroProfissional": "RP-2039",
                  "formacao": "Licenciatura em Matemática"
                }
                """.formatted(funcionarioId);

        String professorResponse = mockMvc.perform(post("/internal/professores")
                        .header("X-Escola-Id", ESCOLA_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(professorRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nomeCompleto").value("Professor Internal Controller Fluxo"))
                .andExpect(jsonPath("$.registroProfissional").value("RP-2039"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID professorId = UUID.fromString(objectMapper.readTree(professorResponse).get("id").asText());

        mockMvc.perform(get("/internal/professores/{id}", professorId)
                        .header("X-Escola-Id", ESCOLA_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(professorId.toString()))
                .andExpect(jsonPath("$.nomeCompleto").value("Professor Internal Controller Fluxo"));

        String alocacaoRequest = """
                {
                  "turmaDisciplinaId": "%s",
                  "dataInicio": "2039-02-01"
                }
                """.formatted(turmaDisciplinaId);

        mockMvc.perform(post("/internal/professores/{id}/turmas-disciplinas", professorId)
                        .header("X-Escola-Id", ESCOLA_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(alocacaoRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.professorId").value(professorId.toString()))
                .andExpect(jsonPath("$.turmaId").value(turmaId.toString()))
                .andExpect(jsonPath("$.disciplinaId").value(disciplinaId.toString()));

        mockMvc.perform(get("/internal/professores/{id}/turmas-disciplinas", professorId)
                        .header("X-Escola-Id", ESCOLA_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].professorId").value(professorId.toString()))
                .andExpect(jsonPath("$[0].disciplinaNome").value("Professor Internal Controller Matematica"));

        mockMvc.perform(get("/internal/professores")
                        .header("X-Escola-Id", ESCOLA_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(professorId.toString()))
                .andExpect(jsonPath("$[0].nomeCompleto").value("Professor Internal Controller Fluxo"));

        mockMvc.perform(get("/internal/professores/turmas/{turmaId}", turmaId)
                        .header("X-Escola-Id", ESCOLA_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].professorId").value(professorId.toString()))
                .andExpect(jsonPath("$[0].turmaId").value(turmaId.toString()));
    }

    @Test
    void deveExigirAutenticacaoNoAdaptadorInterno() throws Exception {
        mockMvc.perform(post("/internal/professores")
                        .header("X-Escola-Id", ESCOLA_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "funcionarioId": "00000000-0000-0000-0000-000000000999"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    private UUID criarFuncionario(String nome, String email) {
        UUID pessoaId = UUID.randomUUID();
        UUID cargoId = UUID.randomUUID();
        UUID funcionarioId = UUID.randomUUID();

        jdbcTemplate.update("""
                INSERT INTO pessoa (id_pessoa, nome_completo, cpf, email, id_escola, ativo, created_at)
                VALUES (?, ?, ?, ?, '00000000-0000-0000-0000-000000000047', true, CURRENT_TIMESTAMP)
                """, pessoaId, nome, cpfAleatorio(), email);

        jdbcTemplate.update("""
                INSERT INTO cargo (id_cargo, codigo, descricao)
                VALUES (?, ?, ?)
                """, cargoId, "PROF-INT-CTRL-" + System.nanoTime(), "Professor");

        jdbcTemplate.update("""
                INSERT INTO funcionario (id_funcionario, id_pessoa, id_cargo, ativo, created_at)
                VALUES (?, ?, ?, true, CURRENT_TIMESTAMP)
                """, funcionarioId, pessoaId, cargoId);

        return funcionarioId;
    }

    private UUID criarPeriodo(String nome, String dataInicio, String dataFim) {
        UUID periodoId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO periodo_letivo
                (id_periodo_letivo, nome, ano, data_inicio, data_fim, ativo, id_escola, created_at)
                VALUES (?, ?, ?, ?, ?, true, '00000000-0000-0000-0000-000000000047', CURRENT_TIMESTAMP)
                """, periodoId, nome, Integer.parseInt(dataInicio.substring(0, 4)), dataInicio, dataFim);
        return periodoId;
    }

    private UUID criarTurma(String codigo, String nome, int capacidade, UUID periodoId) {
        UUID turmaId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO turma
                (id_turma, codigo, nome, capacidade, id_periodo_letivo, id_serie, id_turno, ativo, id_escola, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, true, '00000000-0000-0000-0000-000000000047', CURRENT_TIMESTAMP)
                """, turmaId, codigo, nome, capacidade, periodoId, SERIE_PADRAO_ID, TURNO_MANHA_ID);
        return turmaId;
    }

    private UUID criarDisciplina(String nome, int cargaHoraria) {
        UUID disciplinaId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO disciplina
                (id_disciplina, nome, carga_horaria, ativo, id_escola, created_at)
                VALUES (?, ?, ?, true, '00000000-0000-0000-0000-000000000047', CURRENT_TIMESTAMP)
                """, disciplinaId, nome, cargaHoraria);
        return disciplinaId;
    }

    private UUID vincularDisciplina(UUID turmaId, UUID disciplinaId, int cargaHoraria) {
        UUID turmaDisciplinaId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO turma_disciplina
                (id_turma_disciplina, carga_horaria, created_at, id_disciplina, id_turma)
                VALUES (?, ?, CURRENT_TIMESTAMP, ?, ?)
                """, turmaDisciplinaId, cargaHoraria, disciplinaId, turmaId);
        return turmaDisciplinaId;
    }

    private String cpfAleatorio() {
        return Long.toString(Math.abs(UUID.randomUUID().getMostSignificantBits())).substring(0, 11);
    }
}
