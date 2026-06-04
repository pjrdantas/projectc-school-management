package br.com.escola.historico.adapter.in.web;

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
                "DELETE FROM historico_escolar_item",
                "DELETE FROM historico_escolar",
                "DELETE FROM boletim_item",
                "DELETE FROM boletim",
                "DELETE FROM nota_aluno",
                "DELETE FROM avaliacao",
                "DELETE FROM frequencia_aluno",
                "DELETE FROM frequencia_professor",
                "DELETE FROM aula",
                "DELETE FROM professor_turma_disciplina",
                "DELETE FROM professor",
                "DELETE FROM funcionario WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'boletim.fase24.%')",
                "DELETE FROM cargo WHERE codigo LIKE 'BOLETIM-FASE24-%'",
                "DELETE FROM matricula_etapa",
                "DELETE FROM matricula WHERE id_aluno IN (SELECT id_aluno FROM aluno WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'boletim.fase24.%'))",
                "DELETE FROM aluno WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'boletim.fase24.%')",
                "DELETE FROM pessoa_tipo_pessoa WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'boletim.fase24.%')",
                "DELETE FROM pessoa WHERE email LIKE 'boletim.fase24.%'",
                "DELETE FROM turma_disciplina WHERE id_turma IN (SELECT id_turma FROM turma WHERE codigo LIKE 'BOL24-%')",
                "DELETE FROM disciplina WHERE nome LIKE 'Boletim Fase 24%'",
                "DELETE FROM turma WHERE codigo LIKE 'BOL24-%'",
                "DELETE FROM periodo_letivo WHERE nome LIKE 'BOL24-%'"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
        statements = {
                "DELETE FROM historico_escolar_item",
                "DELETE FROM historico_escolar",
                "DELETE FROM boletim_item",
                "DELETE FROM boletim",
                "DELETE FROM nota_aluno",
                "DELETE FROM avaliacao",
                "DELETE FROM frequencia_aluno",
                "DELETE FROM frequencia_professor",
                "DELETE FROM aula",
                "DELETE FROM professor_turma_disciplina",
                "DELETE FROM professor",
                "DELETE FROM funcionario WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'boletim.fase24.%')",
                "DELETE FROM cargo WHERE codigo LIKE 'BOLETIM-FASE24-%'",
                "DELETE FROM matricula_etapa",
                "DELETE FROM matricula WHERE id_aluno IN (SELECT id_aluno FROM aluno WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'boletim.fase24.%'))",
                "DELETE FROM aluno WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'boletim.fase24.%')",
                "DELETE FROM pessoa_tipo_pessoa WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'boletim.fase24.%')",
                "DELETE FROM pessoa WHERE email LIKE 'boletim.fase24.%'",
                "DELETE FROM turma_disciplina WHERE id_turma IN (SELECT id_turma FROM turma WHERE codigo LIKE 'BOL24-%')",
                "DELETE FROM disciplina WHERE nome LIKE 'Boletim Fase 24%'",
                "DELETE FROM turma WHERE codigo LIKE 'BOL24-%'",
                "DELETE FROM periodo_letivo WHERE nome LIKE 'BOL24-%'"
        },
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class BoletimControllerIntegrationTest {

    private static final UUID SERIE_PADRAO_ID = UUID.fromString("00000000-0000-0000-0000-000000000100");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @WithMockUser
    void deveConsultarBoletimGeradoDaMatricula() throws Exception {
        UUID periodoId = criarPeriodo("BOL24-2043.1", "2043-02-01", "2043-06-30");
        UUID turmaId = criarTurma("BOL24-A", "Boletim Fase 24 Turma A", 30, periodoId);
        UUID disciplinaId = criarDisciplina("Boletim Fase 24 Matematica", 80);
        UUID turmaDisciplinaId = vincularDisciplina(turmaId, disciplinaId, 80);
        UUID funcionarioId = criarFuncionario("Professor Boletim Fase 24", "boletim.fase24.professor@example.com");
        UUID professorId = criarProfessor(funcionarioId);
        UUID alocacaoId = vincularProfessorTurmaDisciplina(professorId, turmaDisciplinaId);
        UUID alunoId = criarAluno("Aluno Boletim Fase 24", "boletim.fase24.aluno@example.com");
        UUID matriculaId = criarMatricula(alunoId, turmaId, periodoId);
        atualizarStatusMatricula(matriculaId, "EFETIVADA", "Aluno com boletim");

        UUID aulaId = criarAula(alocacaoId);
        registrarFrequenciaAluno(aulaId, matriculaId);
        UUID avaliacaoId = criarAvaliacao(alocacaoId);
        lancarNota(avaliacaoId, matriculaId);

        mockMvc.perform(get("/api/matriculas/{matriculaId}/boletim", matriculaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matriculaId").value(matriculaId.toString()))
                .andExpect(jsonPath("$.alunoId").value(alunoId.toString()))
                .andExpect(jsonPath("$.alunoNome").value("Aluno Boletim Fase 24"))
                .andExpect(jsonPath("$.turmaId").value(turmaId.toString()))
                .andExpect(jsonPath("$.persistido").value(false))
                .andExpect(jsonPath("$.periodoLetivoId").value(periodoId.toString()))
                .andExpect(jsonPath("$.indicadores.totalDisciplinas").value(1))
                .andExpect(jsonPath("$.indicadores.mediaGeral").value(8.50))
                .andExpect(jsonPath("$.indicadores.frequenciaGeralPercentual").value(100.00))
                .andExpect(jsonPath("$.indicadores.resultadoGeral").value("APROVADO"))
                .andExpect(jsonPath("$.itens[0].disciplinaId").value(disciplinaId.toString()))
                .andExpect(jsonPath("$.itens[0].disciplinaNome").value("Boletim Fase 24 Matematica"))
                .andExpect(jsonPath("$.itens[0].media").value(8.50))
                .andExpect(jsonPath("$.itens[0].frequenciaPercentual").value(100.00))
                .andExpect(jsonPath("$.itens[0].totalAvaliacoes").value(1))
                .andExpect(jsonPath("$.itens[0].totalFrequencias").value(1))
                .andExpect(jsonPath("$.itens[0].resultado").value("APROVADO"));
    }

    @Test
    @WithMockUser
    void deveFecharEConsultarBoletimPersistidoDaMatricula() throws Exception {
        UUID periodoId = criarPeriodo("BOL24-2043.2", "2043-08-01", "2043-12-20");
        UUID turmaId = criarTurma("BOL24-B", "Boletim Fase 24 Turma B", 30, periodoId);
        UUID disciplinaId = criarDisciplina("Boletim Fase 24 Portugues", 80);
        UUID turmaDisciplinaId = vincularDisciplina(turmaId, disciplinaId, 80);
        UUID funcionarioId = criarFuncionario("Professor Boletim Fase 24 B", "boletim.fase24.professor.b@example.com");
        UUID professorId = criarProfessor(funcionarioId);
        UUID alocacaoId = vincularProfessorTurmaDisciplina(professorId, turmaDisciplinaId);
        UUID alunoId = criarAluno("Aluno Boletim Fase 24 B", "boletim.fase24.aluno.b@example.com");
        UUID matriculaId = criarMatricula(alunoId, turmaId, periodoId);
        atualizarStatusMatricula(matriculaId, "EFETIVADA", "Aluno com boletim persistido");

        UUID aulaId = criarAula(alocacaoId);
        registrarFrequenciaAluno(aulaId, matriculaId);
        UUID avaliacaoId = criarAvaliacao(alocacaoId);
        lancarNota(avaliacaoId, matriculaId);

        String requestBody = """
                {
                  "periodoReferencia": "2043.2",
                  "dataFechamento": "2043-12-21",
                  "observacao": "Fechamento oficial"
                }
                """;

        String fechamentoResponse = mockMvc.perform(post("/api/matriculas/{matriculaId}/boletim/fechamento", matriculaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.boletimId").exists())
                .andExpect(jsonPath("$.matriculaId").value(matriculaId.toString()))
                .andExpect(jsonPath("$.periodoReferencia").value("2043.2"))
                .andExpect(jsonPath("$.dataFechamento").value("2043-12-21"))
                .andExpect(jsonPath("$.observacao").value("Fechamento oficial"))
                .andExpect(jsonPath("$.persistido").value(true))
                .andExpect(jsonPath("$.indicadores.resultadoGeral").value("APROVADO"))
                .andExpect(jsonPath("$.itens[0].disciplinaId").value(disciplinaId.toString()))
                .andExpect(jsonPath("$.itens[0].resultado").value("APROVADO"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID boletimId = UUID.fromString(objectMapper.readTree(fechamentoResponse).get("boletimId").asText());

        mockMvc.perform(post("/api/matriculas/{matriculaId}/boletim/fechamento", matriculaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/matriculas/{matriculaId}/boletim/fechamentos", matriculaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].matriculaId").value(matriculaId.toString()))
                .andExpect(jsonPath("$[0].periodoReferencia").value("2043.2"))
                .andExpect(jsonPath("$[0].persistido").value(true))
                .andExpect(jsonPath("$[0].itens[0].disciplinaId").value(disciplinaId.toString()));

        String geracaoHistoricoRequest = """
                {
                  "boletimId": "%s",
                  "ensinoConcluido": "ENSINO FUNDAMENTAL",
                  "observacoes": "Histórico gerado por boletim fechado"
                }
                """.formatted(boletimId);

        mockMvc.perform(post("/api/historicos-escolares/matriculas/{matriculaId}/geracao-por-boletim", matriculaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(geracaoHistoricoRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nomeAluno").value("Aluno Boletim Fase 24 B"))
                .andExpect(jsonPath("$.anoConclusao").value(2043))
                .andExpect(jsonPath("$.ensinoConcluido").value("ENSINO FUNDAMENTAL"))
                .andExpect(jsonPath("$.observacoes").value("Histórico gerado por boletim fechado"))
                .andExpect(jsonPath("$.componentesCurriculares.length()").value(1))
                .andExpect(jsonPath("$.componentesCurriculares[0].componenteCurricular").value("Boletim Fase 24 Portugues"))
                .andExpect(jsonPath("$.componentesCurriculares[0].anoLetivo").value(2043))
                .andExpect(jsonPath("$.componentesCurriculares[0].serie").exists())
                .andExpect(jsonPath("$.componentesCurriculares[0].notaConceito").value("8.50"))
                .andExpect(jsonPath("$.componentesCurriculares[0].cargaHoraria").value(80));

        mockMvc.perform(post("/api/historicos-escolares/matriculas/{matriculaId}/geracao-por-boletim", matriculaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(geracaoHistoricoRequest))
                .andExpect(status().isConflict());
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
                """, cargoId, "BOLETIM-FASE24-" + System.nanoTime(), "Professor");

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
                  "registroProfissional": "RP-BOL-24",
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
                  "dataInicio": "2043-02-01"
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
                  "dataAula": "2043-03-10",
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
                  "titulo": "Prova boletim fase 24",
                  "dataAplicacao": "2043-03-20",
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
                  "nota": 8.50
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
