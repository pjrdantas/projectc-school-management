package br.com.escola.professor.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
                "MERGE INTO situacao_frequencia (id_situacao_frequencia, codigo, descricao) KEY(codigo) VALUES ('00000000-0000-0000-0000-000000000201', 'PRESENTE', 'Presente')",
                "MERGE INTO situacao_frequencia (id_situacao_frequencia, codigo, descricao) KEY(codigo) VALUES ('00000000-0000-0000-0000-000000000202', 'FALTA', 'Falta')",
                "MERGE INTO situacao_frequencia (id_situacao_frequencia, codigo, descricao) KEY(codigo) VALUES ('00000000-0000-0000-0000-000000000203', 'FALTA_JUSTIFICADA', 'Falta justificada')",
                "DELETE FROM frequencia_aluno",
                "DELETE FROM frequencia_professor",
                "DELETE FROM aula",
                "DELETE FROM professor_turma_disciplina",
                "DELETE FROM professor",
                "DELETE FROM funcionario WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'aula.fase21.%')",
                "DELETE FROM cargo WHERE codigo LIKE 'AULA-FASE21-%'",
                "DELETE FROM matricula_etapa",
                "DELETE FROM matricula WHERE id_aluno IN (SELECT id_aluno FROM aluno WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'aula.fase21.%'))",
                "DELETE FROM aluno WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'aula.fase21.%')",
                "DELETE FROM pessoa_tipo_pessoa WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'aula.fase21.%')",
                "DELETE FROM pessoa WHERE email LIKE 'aula.fase21.%'",
                "DELETE FROM turma_disciplina WHERE id_turma IN (SELECT id_turma FROM turma WHERE codigo LIKE 'AULA21-%')",
                "DELETE FROM disciplina WHERE nome LIKE 'Aula Fase 21%'",
                "DELETE FROM turma WHERE codigo LIKE 'AULA21-%'",
                "DELETE FROM periodo_letivo WHERE nome LIKE 'AULA21-%'"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
        statements = {
                "DELETE FROM frequencia_aluno",
                "DELETE FROM frequencia_professor",
                "DELETE FROM aula",
                "DELETE FROM professor_turma_disciplina",
                "DELETE FROM professor",
                "DELETE FROM funcionario WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'aula.fase21.%')",
                "DELETE FROM cargo WHERE codigo LIKE 'AULA-FASE21-%'",
                "DELETE FROM matricula_etapa",
                "DELETE FROM matricula WHERE id_aluno IN (SELECT id_aluno FROM aluno WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'aula.fase21.%'))",
                "DELETE FROM aluno WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'aula.fase21.%')",
                "DELETE FROM pessoa_tipo_pessoa WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'aula.fase21.%')",
                "DELETE FROM pessoa WHERE email LIKE 'aula.fase21.%'",
                "DELETE FROM turma_disciplina WHERE id_turma IN (SELECT id_turma FROM turma WHERE codigo LIKE 'AULA21-%')",
                "DELETE FROM disciplina WHERE nome LIKE 'Aula Fase 21%'",
                "DELETE FROM turma WHERE codigo LIKE 'AULA21-%'",
                "DELETE FROM periodo_letivo WHERE nome LIKE 'AULA21-%'"
        },
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class AulaControllerIntegrationTest {

    private static final UUID SERIE_PADRAO_ID = UUID.fromString("00000000-0000-0000-0000-000000000100");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @WithMockUser
    void deveCriarAulaERegistrarFrequenciasDoProfessorEAluno() throws Exception {
        UUID funcionarioId = criarFuncionario("Professor Aula Fase 21", "aula.fase21.professor@example.com");
        UUID periodoId = criarPeriodo("AULA21-2038.1", "2038-02-01", "2038-06-30");
        UUID turmaId = criarTurma("AULA21-A", "Aula Fase 21 Turma A", 30, periodoId);
        UUID disciplinaId = criarDisciplina("Aula Fase 21 Ciencias", 80);
        UUID turmaDisciplinaId = vincularDisciplina(turmaId, disciplinaId, 80);
        UUID professorId = criarProfessor(funcionarioId);
        UUID alocacaoId = vincularProfessorTurmaDisciplina(professorId, turmaDisciplinaId);
        UUID alunoId = criarAluno("Aluno Aula Fase 21", "aula.fase21.aluno@example.com");
        UUID matriculaId = criarMatricula(alunoId, turmaId, periodoId);
        atualizarStatusMatricula(matriculaId, "EFETIVADA", "Aluno apto para frequência");

        String aulaRequest = """
                {
                  "professorTurmaDisciplinaId": "%s",
                  "dataAula": "2038-03-10",
                  "horarioInicio": "07:30:00",
                  "horarioFim": "08:20:00",
                  "conteudoMinistrado": "Introdução ao conteúdo da fase 21",
                  "realizada": true
                }
                """.formatted(alocacaoId);

        String aulaResponse = mockMvc.perform(post("/api/aulas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(aulaRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.professorTurmaDisciplinaId").value(alocacaoId.toString()))
                .andExpect(jsonPath("$.professorId").value(professorId.toString()))
                .andExpect(jsonPath("$.turmaId").value(turmaId.toString()))
                .andExpect(jsonPath("$.disciplinaId").value(disciplinaId.toString()))
                .andExpect(jsonPath("$.dataAula").value("2038-03-10"))
                .andExpect(jsonPath("$.realizada").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID aulaId = UUID.fromString(objectMapper.readTree(aulaResponse).get("id").asText());

        mockMvc.perform(get("/api/aulas")
                        .param("turmaId", turmaId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(aulaId.toString()))
                .andExpect(jsonPath("$[0].turmaNome").value("Aula Fase 21 Turma A"));

        String frequenciaProfessorRequest = """
                {
                  "presente": true,
                  "justificativa": "Professor presente"
                }
                """;

        mockMvc.perform(post("/api/aulas/{id}/frequencia-professor", aulaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(frequenciaProfessorRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.aulaId").value(aulaId.toString()))
                .andExpect(jsonPath("$.professorId").value(professorId.toString()))
                .andExpect(jsonPath("$.presente").value(true));

        mockMvc.perform(post("/api/aulas/{id}/frequencia-professor", aulaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(frequenciaProfessorRequest))
                .andExpect(status().isConflict());

        String frequenciaAlunoRequest = """
                {
                  "matriculaId": "%s",
                  "situacao": "PRESENTE",
                  "justificativa": "Aluno presente"
                }
                """.formatted(matriculaId);

        mockMvc.perform(post("/api/aulas/{id}/frequencias-alunos", aulaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(frequenciaAlunoRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.aulaId").value(aulaId.toString()))
                .andExpect(jsonPath("$.matriculaId").value(matriculaId.toString()))
                .andExpect(jsonPath("$.alunoId").value(alunoId.toString()))
                .andExpect(jsonPath("$.situacao").value("PRESENTE"));

        mockMvc.perform(get("/api/aulas/{id}/frequencias-alunos", aulaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].matriculaId").value(matriculaId.toString()))
                .andExpect(jsonPath("$[0].alunoNome").value("Aluno Aula Fase 21"));
    }

    @Test
    @WithMockUser
    void deveBloquearFrequenciaAlunoDeOutraTurma() throws Exception {
        UUID funcionarioId = criarFuncionario("Professor Aula Fase 21 Bloqueio", "aula.fase21.bloqueio.professor@example.com");
        UUID periodoId = criarPeriodo("AULA21-2039.1", "2039-02-01", "2039-06-30");
        UUID turmaId = criarTurma("AULA21-B", "Aula Fase 21 Turma B", 30, periodoId);
        UUID outraTurmaId = criarTurma("AULA21-C", "Aula Fase 21 Turma C", 30, periodoId);
        UUID disciplinaId = criarDisciplina("Aula Fase 21 Geografia", 80);
        UUID turmaDisciplinaId = vincularDisciplina(turmaId, disciplinaId, 80);
        UUID professorId = criarProfessor(funcionarioId);
        UUID alocacaoId = vincularProfessorTurmaDisciplina(professorId, turmaDisciplinaId);
        UUID alunoId = criarAluno("Aluno Aula Fase 21 Outra Turma", "aula.fase21.outra-turma@example.com");
        UUID matriculaId = criarMatricula(alunoId, outraTurmaId, periodoId);

        UUID aulaId = criarAula(alocacaoId);

        String frequenciaAlunoRequest = """
                {
                  "matriculaId": "%s",
                  "situacao": "PRESENTE"
                }
                """.formatted(matriculaId);

        mockMvc.perform(post("/api/aulas/{id}/frequencias-alunos", aulaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(frequenciaAlunoRequest))
                .andExpect(status().isBadRequest());
    }

    private UUID criarFuncionario(String nome, String email) {
        UUID pessoaId = UUID.randomUUID();
        UUID cargoId = UUID.randomUUID();
        UUID funcionarioId = UUID.randomUUID();

        jdbcTemplate.update("""
                INSERT INTO pessoa (id_pessoa, nome_completo, cpf, email, ativo, created_at)
                VALUES (?, ?, ?, ?, true, CURRENT_TIMESTAMP)
                """, pessoaId, nome, cpfAleatorio(), email);

        jdbcTemplate.update("""
                INSERT INTO cargo (id_cargo, codigo, descricao)
                VALUES (?, ?, ?)
                """, cargoId, "AULA-FASE21-" + System.nanoTime(), "Professor");

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

    private UUID criarProfessor(UUID funcionarioId) throws Exception {
        String requestBody = """
                {
                  "funcionarioId": "%s",
                  "registroProfissional": "RP-AULA-21",
                  "formacao": "Licenciatura"
                }
                """.formatted(funcionarioId);

        String responseBody = mockMvc.perform(post("/api/professores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());
    }

    private UUID vincularProfessorTurmaDisciplina(UUID professorId, UUID turmaDisciplinaId) throws Exception {
        String requestBody = """
                {
                  "turmaDisciplinaId": "%s",
                  "dataInicio": "2038-02-01"
                }
                """.formatted(turmaDisciplinaId);

        String responseBody = mockMvc.perform(post("/api/professores/{id}/turmas-disciplinas", professorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());
    }

    private UUID criarAluno(String nome, String email) throws Exception {
        String requestBody = """
                {
                  "nomeCompleto": "%s",
                  "cpf": "%s",
                  "email": "%s",
                  "dataNascimento": "2011-04-15"
                }
                """.formatted(nome, cpfAleatorio(), email);

        String responseBody = mockMvc.perform(post("/api/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());
    }

    private UUID criarMatricula(UUID alunoId, UUID turmaId, UUID periodoId) throws Exception {
        String requestBody = """
                {
                  "alunoId": "%s",
                  "turmaId": "%s",
                  "periodoLetivoId": "%s",
                  "tipoMatricula": "PRIMEIRA_MATRICULA"
                }
                """.formatted(alunoId, turmaId, periodoId);

        String responseBody = mockMvc.perform(post("/api/matriculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());
    }

    private void atualizarStatusMatricula(UUID matriculaId, String novoStatus, String justificativa) throws Exception {
        String requestBody = """
                {
                  "status": "%s",
                  "justificativa": "%s"
                }
                """.formatted(novoStatus, justificativa);

        mockMvc.perform(patch("/api/matriculas/{id}/status", matriculaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(novoStatus));
    }

    private UUID criarAula(UUID alocacaoId) throws Exception {
        String requestBody = """
                {
                  "professorTurmaDisciplinaId": "%s",
                  "dataAula": "2039-03-10",
                  "realizada": true
                }
                """.formatted(alocacaoId);

        String responseBody = mockMvc.perform(post("/api/aulas")
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
