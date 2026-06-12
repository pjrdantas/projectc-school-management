package br.com.escola.avaliacao.adapter.in.web;

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
                "MERGE INTO tipo_avaliacao (id_tipo_avaliacao, codigo, descricao) KEY(codigo) VALUES ('00000000-0000-0000-0000-000000000301', 'PROVA', 'Prova')",
                "MERGE INTO tipo_avaliacao (id_tipo_avaliacao, codigo, descricao) KEY(codigo) VALUES ('00000000-0000-0000-0000-000000000302', 'TRABALHO', 'Trabalho')",
                "DELETE FROM nota_aluno",
                "DELETE FROM avaliacao",
                "DELETE FROM professor_turma_disciplina",
                "DELETE FROM professor",
                "DELETE FROM funcionario WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'avaliacao.fase22.%')",
                "DELETE FROM cargo WHERE codigo LIKE 'AVAL-FASE22-%'",
                "DELETE FROM matricula_etapa",
                "DELETE FROM matricula WHERE id_aluno IN (SELECT id_aluno FROM aluno WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'avaliacao.fase22.%'))",
                "DELETE FROM aluno WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'avaliacao.fase22.%')",
                "DELETE FROM pessoa_tipo_pessoa WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'avaliacao.fase22.%')",
                "DELETE FROM pessoa WHERE email LIKE 'avaliacao.fase22.%'",
                "DELETE FROM turma_disciplina WHERE id_turma IN (SELECT id_turma FROM turma WHERE codigo LIKE 'AVAL22-%')",
                "DELETE FROM disciplina WHERE nome LIKE 'Avaliacao Fase 22%'",
                "DELETE FROM turma WHERE codigo LIKE 'AVAL22-%'",
                "DELETE FROM periodo_letivo WHERE nome LIKE 'AVAL22-%'"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
        statements = {
                "DELETE FROM nota_aluno",
                "DELETE FROM avaliacao",
                "DELETE FROM professor_turma_disciplina",
                "DELETE FROM professor",
                "DELETE FROM funcionario WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'avaliacao.fase22.%')",
                "DELETE FROM cargo WHERE codigo LIKE 'AVAL-FASE22-%'",
                "DELETE FROM matricula_etapa",
                "DELETE FROM matricula WHERE id_aluno IN (SELECT id_aluno FROM aluno WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'avaliacao.fase22.%'))",
                "DELETE FROM aluno WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'avaliacao.fase22.%')",
                "DELETE FROM pessoa_tipo_pessoa WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'avaliacao.fase22.%')",
                "DELETE FROM pessoa WHERE email LIKE 'avaliacao.fase22.%'",
                "DELETE FROM turma_disciplina WHERE id_turma IN (SELECT id_turma FROM turma WHERE codigo LIKE 'AVAL22-%')",
                "DELETE FROM disciplina WHERE nome LIKE 'Avaliacao Fase 22%'",
                "DELETE FROM turma WHERE codigo LIKE 'AVAL22-%'",
                "DELETE FROM periodo_letivo WHERE nome LIKE 'AVAL22-%'"
        },
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class AvaliacaoControllerIntegrationTest {

    private static final UUID SERIE_PADRAO_ID = UUID.fromString("00000000-0000-0000-0000-000000000100");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @WithMockUser
    void deveCriarAvaliacaoELancarNotaDoAluno() throws Exception {
        UUID periodoId = criarPeriodo("AVAL22-2040.1", "2040-02-01", "2040-06-30");
        UUID turmaId = criarTurma("AVAL22-A", "Avaliacao Fase 22 Turma A", 30, periodoId);
        UUID disciplinaId = criarDisciplina("Avaliacao Fase 22 Matematica", 80);
        UUID turmaDisciplinaId = vincularDisciplina(turmaId, disciplinaId, 80);
        UUID funcionarioId = criarFuncionario("Professor Avaliacao Fase 22", "avaliacao.fase22.professor@example.com");
        UUID professorId = criarProfessor(funcionarioId);
        UUID alocacaoId = vincularProfessorTurmaDisciplina(professorId, turmaDisciplinaId);
        UUID alunoId = criarAluno("Aluno Avaliacao Fase 22", "avaliacao.fase22.aluno@example.com");
        UUID matriculaId = criarMatricula(alunoId, turmaId, periodoId);
        atualizarStatusMatricula(matriculaId, "EFETIVADA", "Aluno apto para nota");

        String avaliacaoRequest = """
                {
                  "professorTurmaDisciplinaId": "%s",
                  "titulo": "Prova bimestral fase 22",
                  "descricao": "Avaliação do primeiro bimestre",
                  "dataAplicacao": "2040-03-20",
                  "valorMaximo": 10.00,
                  "peso": 1.00,
                  "tipoAvaliacao": "PROVA"
                }
                """.formatted(alocacaoId);

        String avaliacaoResponse = mockMvc.perform(post("/api/avaliacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(avaliacaoRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.professorTurmaDisciplinaId").value(alocacaoId.toString()))
                .andExpect(jsonPath("$.professorId").value(professorId.toString()))
                .andExpect(jsonPath("$.turmaId").value(turmaId.toString()))
                .andExpect(jsonPath("$.disciplinaId").value(disciplinaId.toString()))
                .andExpect(jsonPath("$.titulo").value("Prova bimestral fase 22"))
                .andExpect(jsonPath("$.tipoAvaliacao").value("PROVA"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID avaliacaoId = UUID.fromString(objectMapper.readTree(avaliacaoResponse).get("id").asText());

        mockMvc.perform(get("/api/avaliacoes")
                        .param("turmaId", turmaId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(avaliacaoId.toString()))
                .andExpect(jsonPath("$[0].turmaNome").value("Avaliacao Fase 22 Turma A"));

        String notaRequest = """
                {
                  "matriculaId": "%s",
                  "nota": 8.50,
                  "observacao": "Boa participação"
                }
                """.formatted(matriculaId);

        mockMvc.perform(post("/api/avaliacoes/{id}/notas", avaliacaoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(notaRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.avaliacaoId").value(avaliacaoId.toString()))
                .andExpect(jsonPath("$.matriculaId").value(matriculaId.toString()))
                .andExpect(jsonPath("$.alunoId").value(alunoId.toString()))
                .andExpect(jsonPath("$.alunoNome").value("Aluno Avaliacao Fase 22"))
                .andExpect(jsonPath("$.nota").value(8.50));

        mockMvc.perform(post("/api/avaliacoes/{id}/notas", avaliacaoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(notaRequest))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/avaliacoes/{id}/notas", avaliacaoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].matriculaId").value(matriculaId.toString()))
                .andExpect(jsonPath("$[0].avaliacaoTitulo").value("Prova bimestral fase 22"));

        mockMvc.perform(get("/api/matriculas/{matriculaId}/notas", matriculaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].avaliacaoId").value(avaliacaoId.toString()))
                .andExpect(jsonPath("$[0].nota").value(8.50));
    }

    @Test
    @WithMockUser
    void deveBloquearNotaAcimaDoValorMaximo() throws Exception {
        UUID periodoId = criarPeriodo("AVAL22-2041.1", "2041-02-01", "2041-06-30");
        UUID turmaId = criarTurma("AVAL22-B", "Avaliacao Fase 22 Turma B", 30, periodoId);
        UUID disciplinaId = criarDisciplina("Avaliacao Fase 22 Ciencias", 80);
        UUID turmaDisciplinaId = vincularDisciplina(turmaId, disciplinaId, 80);
        UUID funcionarioId = criarFuncionario("Professor Avaliacao Fase 22 Bloqueio", "avaliacao.fase22.bloqueio.professor@example.com");
        UUID professorId = criarProfessor(funcionarioId);
        UUID alocacaoId = vincularProfessorTurmaDisciplina(professorId, turmaDisciplinaId);
        UUID alunoId = criarAluno("Aluno Avaliacao Fase 22 Bloqueio", "avaliacao.fase22.bloqueio.aluno@example.com");
        UUID matriculaId = criarMatricula(alunoId, turmaId, periodoId);
        UUID avaliacaoId = criarAvaliacao(alocacaoId, "Prova valor máximo");

        String notaRequest = """
                {
                  "matriculaId": "%s",
                  "nota": 11.00
                }
                """.formatted(matriculaId);

        mockMvc.perform(post("/api/avaliacoes/{id}/notas", avaliacaoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(notaRequest))
                .andExpect(status().isBadRequest());
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
                """, cargoId, "AVAL-FASE22-" + System.nanoTime(), "Professor");

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
                  "registroProfissional": "RP-AVAL-22",
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
                  "dataInicio": "2040-02-01"
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

    private UUID criarAvaliacao(UUID alocacaoId, String titulo) throws Exception {
        String requestBody = """
                {
                  "professorTurmaDisciplinaId": "%s",
                  "titulo": "%s",
                  "dataAplicacao": "2041-03-20",
                  "valorMaximo": 10.00,
                  "peso": 1.00,
                  "tipoAvaliacao": "PROVA"
                }
                """.formatted(alocacaoId, titulo);

        String responseBody = mockMvc.perform(post("/api/avaliacoes")
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
