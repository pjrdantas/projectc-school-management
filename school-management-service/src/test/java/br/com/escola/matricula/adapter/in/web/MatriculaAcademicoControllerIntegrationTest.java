package br.com.escola.matricula.adapter.in.web;

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
                "MERGE INTO tipo_avaliacao (id_tipo_avaliacao, codigo, descricao) KEY(codigo) VALUES ('00000000-0000-0000-0000-000000000301', 'PROVA', 'Prova')",
                "DELETE FROM nota_aluno",
                "DELETE FROM avaliacao",
                "DELETE FROM frequencia_aluno",
                "DELETE FROM frequencia_professor",
                "DELETE FROM aula",
                "DELETE FROM professor_turma_disciplina",
                "DELETE FROM professor",
                "DELETE FROM funcionario WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'academico.fase23.%')",
                "DELETE FROM cargo WHERE codigo LIKE 'ACADEMICO-FASE23-%'",
                "DELETE FROM matricula_etapa",
                "DELETE FROM matricula WHERE id_aluno IN (SELECT id_aluno FROM aluno WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'academico.fase23.%'))",
                "DELETE FROM aluno WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'academico.fase23.%')",
                "DELETE FROM pessoa_tipo_pessoa WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'academico.fase23.%')",
                "DELETE FROM pessoa WHERE email LIKE 'academico.fase23.%'",
                "DELETE FROM turma_disciplina WHERE id_turma IN (SELECT id_turma FROM turma WHERE codigo LIKE 'ACAD23-%')",
                "DELETE FROM disciplina WHERE nome LIKE 'Academico Fase 23%'",
                "DELETE FROM turma WHERE codigo LIKE 'ACAD23-%'",
                "DELETE FROM periodo_letivo WHERE nome LIKE 'ACAD23-%'"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
        statements = {
                "DELETE FROM nota_aluno",
                "DELETE FROM avaliacao",
                "DELETE FROM frequencia_aluno",
                "DELETE FROM frequencia_professor",
                "DELETE FROM aula",
                "DELETE FROM professor_turma_disciplina",
                "DELETE FROM professor",
                "DELETE FROM funcionario WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'academico.fase23.%')",
                "DELETE FROM cargo WHERE codigo LIKE 'ACADEMICO-FASE23-%'",
                "DELETE FROM matricula_etapa",
                "DELETE FROM matricula WHERE id_aluno IN (SELECT id_aluno FROM aluno WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'academico.fase23.%'))",
                "DELETE FROM aluno WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'academico.fase23.%')",
                "DELETE FROM pessoa_tipo_pessoa WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'academico.fase23.%')",
                "DELETE FROM pessoa WHERE email LIKE 'academico.fase23.%'",
                "DELETE FROM turma_disciplina WHERE id_turma IN (SELECT id_turma FROM turma WHERE codigo LIKE 'ACAD23-%')",
                "DELETE FROM disciplina WHERE nome LIKE 'Academico Fase 23%'",
                "DELETE FROM turma WHERE codigo LIKE 'ACAD23-%'",
                "DELETE FROM periodo_letivo WHERE nome LIKE 'ACAD23-%'"
        },
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class MatriculaAcademicoControllerIntegrationTest {

    private static final UUID SERIE_PADRAO_ID = UUID.fromString("00000000-0000-0000-0000-000000000100");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @WithMockUser
    void deveConsultarResumoAcademicoDaMatriculaComNotasEFrequencias() throws Exception {
        UUID periodoId = criarPeriodo("ACAD23-2042.1", "2042-02-01", "2042-06-30");
        UUID turmaId = criarTurma("ACAD23-A", "Academico Fase 23 Turma A", 30, periodoId);
        UUID disciplinaId = criarDisciplina("Academico Fase 23 Matematica", 80);
        UUID turmaDisciplinaId = vincularDisciplina(turmaId, disciplinaId, 80);
        UUID funcionarioId = criarFuncionario("Professor Academico Fase 23", "academico.fase23.professor@example.com");
        UUID professorId = criarProfessor(funcionarioId);
        UUID alocacaoId = vincularProfessorTurmaDisciplina(professorId, turmaDisciplinaId);
        UUID alunoId = criarAluno("Aluno Academico Fase 23", "academico.fase23.aluno@example.com");
        UUID matriculaId = criarMatricula(alunoId, turmaId, periodoId);
        atualizarStatusMatricula(matriculaId, "EFETIVADA", "Aluno com registros academicos");

        UUID aulaId = criarAula(alocacaoId);
        registrarFrequenciaAluno(aulaId, matriculaId);
        UUID avaliacaoId = criarAvaliacao(alocacaoId);
        lancarNota(avaliacaoId, matriculaId);

        mockMvc.perform(get("/api/matriculas/{matriculaId}/academico", matriculaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matriculaId").value(matriculaId.toString()))
                .andExpect(jsonPath("$.alunoId").value(alunoId.toString()))
                .andExpect(jsonPath("$.alunoNome").value("Aluno Academico Fase 23"))
                .andExpect(jsonPath("$.turmaId").value(turmaId.toString()))
                .andExpect(jsonPath("$.periodoLetivoId").value(periodoId.toString()))
                .andExpect(jsonPath("$.status").value("EFETIVADA"))
                .andExpect(jsonPath("$.indicadores.totalFrequencias").value(1))
                .andExpect(jsonPath("$.indicadores.presencas").value(1))
                .andExpect(jsonPath("$.indicadores.faltas").value(0))
                .andExpect(jsonPath("$.indicadores.totalNotas").value(1))
                .andExpect(jsonPath("$.indicadores.mediaNotas").value(8.50))
                .andExpect(jsonPath("$.indicadores.mediaPercentual").value(85.00))
                .andExpect(jsonPath("$.frequencias[0].aulaId").value(aulaId.toString()))
                .andExpect(jsonPath("$.frequencias[0].disciplinaNome").value("Academico Fase 23 Matematica"))
                .andExpect(jsonPath("$.frequencias[0].situacao").value("PRESENTE"))
                .andExpect(jsonPath("$.notas[0].avaliacaoId").value(avaliacaoId.toString()))
                .andExpect(jsonPath("$.notas[0].avaliacaoTitulo").value("Prova academica fase 23"))
                .andExpect(jsonPath("$.notas[0].nota").value(8.50));
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
                """, cargoId, "ACADEMICO-FASE23-" + System.nanoTime(), "Professor");

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
                  "registroProfissional": "RP-ACAD-23",
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
                  "dataInicio": "2042-02-01"
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
                  "dataAula": "2042-03-10",
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

    private void registrarFrequenciaAluno(UUID aulaId, UUID matriculaId) throws Exception {
        String requestBody = """
                {
                  "matriculaId": "%s",
                  "situacao": "PRESENTE"
                }
                """.formatted(matriculaId);

        mockMvc.perform(post("/api/aulas/{id}/frequencias-alunos", aulaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());
    }

    private UUID criarAvaliacao(UUID alocacaoId) throws Exception {
        String requestBody = """
                {
                  "professorTurmaDisciplinaId": "%s",
                  "titulo": "Prova academica fase 23",
                  "dataAplicacao": "2042-03-20",
                  "valorMaximo": 10.00,
                  "peso": 1.00,
                  "tipoAvaliacao": "PROVA"
                }
                """.formatted(alocacaoId);

        String responseBody = mockMvc.perform(post("/api/avaliacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());
    }

    private void lancarNota(UUID avaliacaoId, UUID matriculaId) throws Exception {
        String requestBody = """
                {
                  "matriculaId": "%s",
                  "nota": 8.50,
                  "observacao": "Bom desempenho"
                }
                """.formatted(matriculaId);

        mockMvc.perform(post("/api/avaliacoes/{id}/notas", avaliacaoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());
    }

    private String cpfAleatorio() {
        long cpf = System.nanoTime() % 1_000_000_00000L;
        return String.format("%011d", cpf);
    }
}
