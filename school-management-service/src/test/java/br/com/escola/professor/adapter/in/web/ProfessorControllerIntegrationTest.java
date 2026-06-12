package br.com.escola.professor.adapter.in.web;

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
                "DELETE FROM funcionario WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'professor.fase20.%')",
                "DELETE FROM cargo WHERE codigo LIKE 'PROF-FASE20-%'",
                "DELETE FROM pessoa WHERE email LIKE 'professor.fase20.%'",
                "DELETE FROM turma_disciplina WHERE id_turma IN (SELECT id_turma FROM turma WHERE codigo LIKE 'PROF20-%')",
                "DELETE FROM disciplina WHERE nome LIKE 'Professor Fase 20%'",
                "DELETE FROM turma WHERE codigo LIKE 'PROF20-%'",
                "DELETE FROM periodo_letivo WHERE nome LIKE 'PROF20-%'"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
        statements = {
                "DELETE FROM professor_turma_disciplina",
                "DELETE FROM professor",
                "DELETE FROM funcionario WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'professor.fase20.%')",
                "DELETE FROM cargo WHERE codigo LIKE 'PROF-FASE20-%'",
                "DELETE FROM pessoa WHERE email LIKE 'professor.fase20.%'",
                "DELETE FROM turma_disciplina WHERE id_turma IN (SELECT id_turma FROM turma WHERE codigo LIKE 'PROF20-%')",
                "DELETE FROM disciplina WHERE nome LIKE 'Professor Fase 20%'",
                "DELETE FROM turma WHERE codigo LIKE 'PROF20-%'",
                "DELETE FROM periodo_letivo WHERE nome LIKE 'PROF20-%'"
        },
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class ProfessorControllerIntegrationTest {

    private static final UUID SERIE_PADRAO_ID = UUID.fromString("00000000-0000-0000-0000-000000000100");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @WithMockUser
    void deveCadastrarProfessorEVincularTurmaDisciplina() throws Exception {
        UUID funcionarioId = criarFuncionario("Professor Fase 20 Fluxo", "professor.fase20.fluxo@example.com");
        UUID periodoId = criarPeriodo("PROF20-2037.1", "2037-02-01", "2037-06-30");
        UUID turmaId = criarTurma("PROF20-A", "Professor Fase 20 Turma A", 30, periodoId);
        UUID disciplinaId = criarDisciplina("Professor Fase 20 Matematica", 80);
        UUID turmaDisciplinaId = vincularDisciplina(turmaId, disciplinaId, 80);

        String professorRequest = """
                {
                  "funcionarioId": "%s",
                  "registroProfissional": "RP-2037",
                  "formacao": "Licenciatura em Matemática"
                }
                """.formatted(funcionarioId);

        String professorResponse = mockMvc.perform(post("/api/professores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(professorRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nomeCompleto").value("Professor Fase 20 Fluxo"))
                .andExpect(jsonPath("$.registroProfissional").value("RP-2037"))
                .andExpect(jsonPath("$.formacao").value("Licenciatura em Matemática"))
                .andExpect(jsonPath("$.ativo").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID professorId = UUID.fromString(objectMapper.readTree(professorResponse).get("id").asText());

        mockMvc.perform(get("/api/professores/{id}", professorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(professorId.toString()))
                .andExpect(jsonPath("$.nomeCompleto").value("Professor Fase 20 Fluxo"));

        String alocacaoRequest = """
                {
                  "turmaDisciplinaId": "%s",
                  "dataInicio": "2037-02-01"
                }
                """.formatted(turmaDisciplinaId);

        mockMvc.perform(post("/api/professores/{id}/turmas-disciplinas", professorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(alocacaoRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.professorId").value(professorId.toString()))
                .andExpect(jsonPath("$.professorNome").value("Professor Fase 20 Fluxo"))
                .andExpect(jsonPath("$.turmaDisciplinaId").value(turmaDisciplinaId.toString()))
                .andExpect(jsonPath("$.turmaId").value(turmaId.toString()))
                .andExpect(jsonPath("$.turmaNome").value("Professor Fase 20 Turma A"))
                .andExpect(jsonPath("$.disciplinaId").value(disciplinaId.toString()))
                .andExpect(jsonPath("$.disciplinaNome").value("Professor Fase 20 Matematica"))
                .andExpect(jsonPath("$.ativo").value(true));

        mockMvc.perform(post("/api/professores/{id}/turmas-disciplinas", professorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(alocacaoRequest))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/professores/{id}/turmas-disciplinas", professorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].professorId").value(professorId.toString()))
                .andExpect(jsonPath("$[0].disciplinaNome").value("Professor Fase 20 Matematica"));

        mockMvc.perform(get("/api/turmas/{turmaId}/professores", turmaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].professorId").value(professorId.toString()))
                .andExpect(jsonPath("$[0].turmaId").value(turmaId.toString()));
    }

    @Test
    @WithMockUser
    void deveBloquearProfessorDuplicadoParaMesmoFuncionario() throws Exception {
        UUID funcionarioId = criarFuncionario("Professor Fase 20 Duplicado", "professor.fase20.duplicado@example.com");

        String request = """
                {
                  "funcionarioId": "%s",
                  "registroProfissional": "RP-DUP"
                }
                """.formatted(funcionarioId);

        mockMvc.perform(post("/api/professores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/professores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict());
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
                """, cargoId, "PROF-FASE20-" + System.nanoTime(), "Professor");

        jdbcTemplate.update("""
                INSERT INTO funcionario (id_funcionario, id_pessoa, id_cargo, ativo, created_at)
                VALUES (?, ?, ?, true, CURRENT_TIMESTAMP)
                """, funcionarioId, pessoaId, cargoId);

        return funcionarioId;
    }

    private UUID criarPeriodo(String nome, String dataInicio, String dataFim) throws Exception {
        String requestBody = """
                {
                  "nome": "%s",
                  "dataInicio": "%s",
                  "dataFim": "%s"
                }
                """.formatted(nome, dataInicio, dataFim);

        String responseBody = mockMvc.perform(post("/api/periodos-letivos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());
    }

    private UUID criarTurma(String codigo, String nome, int capacidade, UUID periodoId) throws Exception {
        String requestBody = """
                {
                  "codigo": "%s",
                  "nome": "%s",
                  "capacidade": %d,
                  "periodoLetivoId": "%s",
                  "serieId": "%s",
                  "turno": "MANHA",
                  "status": "ATIVA"
                }
                """.formatted(codigo, nome, capacidade, periodoId, SERIE_PADRAO_ID);

        String responseBody = mockMvc.perform(post("/api/turmas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());
    }

    private UUID criarDisciplina(String nome, int cargaHoraria) throws Exception {
        String requestBody = """
                {
                  "nome": "%s",
                  "cargaHoraria": %d,
                  "status": "ATIVA"
                }
                """.formatted(nome, cargaHoraria);

        String responseBody = mockMvc.perform(post("/api/disciplinas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());
    }

    private UUID vincularDisciplina(UUID turmaId, UUID disciplinaId, int cargaHoraria) throws Exception {
        String requestBody = """
                {
                  "disciplinaId": "%s",
                  "cargaHoraria": %d
                }
                """.formatted(disciplinaId, cargaHoraria);

        String responseBody = mockMvc.perform(post("/api/turmas/{turmaId}/disciplinas", turmaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());
    }

    private String cpfAleatorio() {
        long cpf = System.nanoTime() % 1_000_000_00000L;
        return String.format("%011d", cpf);
    }
}
