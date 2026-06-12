package br.com.escola.planejamento.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
                "MERGE INTO status_planejamento (id_status_planejamento, codigo, descricao) KEY(codigo) VALUES ('00000000-0000-0000-0000-000000000401', 'RASCUNHO', 'Rascunho')",
                "MERGE INTO status_planejamento (id_status_planejamento, codigo, descricao) KEY(codigo) VALUES ('00000000-0000-0000-0000-000000000402', 'EM_ANALISE', 'Em análise')",
                "MERGE INTO status_planejamento (id_status_planejamento, codigo, descricao) KEY(codigo) VALUES ('00000000-0000-0000-0000-000000000403', 'APROVADO', 'Aprovado')",
                "MERGE INTO status_planejamento (id_status_planejamento, codigo, descricao) KEY(codigo) VALUES ('00000000-0000-0000-0000-000000000404', 'REPROVADO', 'Reprovado')",
                "MERGE INTO tipo_avaliacao (id_tipo_avaliacao, codigo, descricao) KEY(codigo) VALUES ('00000000-0000-0000-0000-000000000301', 'PROVA', 'Prova')",
                "DELETE FROM planejamento_bimestral_avaliacao",
                "DELETE FROM planejamento_bimestral_aula",
                "DELETE FROM planejamento_bimestral",
                "DELETE FROM periodo_avaliativo WHERE id_periodo_letivo IN (SELECT id_periodo_letivo FROM periodo_letivo WHERE nome LIKE 'PLAN46A-%')",
                "DELETE FROM professor_turma_disciplina",
                "DELETE FROM professor",
                "DELETE FROM funcionario WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'planejamento.fase46a.%')",
                "DELETE FROM cargo WHERE codigo LIKE 'PLAN-FASE46A-%'",
                "DELETE FROM pessoa_tipo_pessoa WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'planejamento.fase46a.%')",
                "DELETE FROM pessoa WHERE email LIKE 'planejamento.fase46a.%'",
                "DELETE FROM turma_disciplina WHERE id_turma IN (SELECT id_turma FROM turma WHERE codigo LIKE 'PLAN46A-%')",
                "DELETE FROM disciplina WHERE nome LIKE 'Planejamento Fase 46A%'",
                "DELETE FROM turma WHERE codigo LIKE 'PLAN46A-%'",
                "DELETE FROM periodo_letivo WHERE nome LIKE 'PLAN46A-%'"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
        statements = {
                "DELETE FROM planejamento_bimestral_avaliacao",
                "DELETE FROM planejamento_bimestral_aula",
                "DELETE FROM planejamento_bimestral",
                "DELETE FROM periodo_avaliativo WHERE id_periodo_letivo IN (SELECT id_periodo_letivo FROM periodo_letivo WHERE nome LIKE 'PLAN46A-%')",
                "DELETE FROM professor_turma_disciplina",
                "DELETE FROM professor",
                "DELETE FROM funcionario WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'planejamento.fase46a.%')",
                "DELETE FROM cargo WHERE codigo LIKE 'PLAN-FASE46A-%'",
                "DELETE FROM pessoa_tipo_pessoa WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'planejamento.fase46a.%')",
                "DELETE FROM pessoa WHERE email LIKE 'planejamento.fase46a.%'",
                "DELETE FROM turma_disciplina WHERE id_turma IN (SELECT id_turma FROM turma WHERE codigo LIKE 'PLAN46A-%')",
                "DELETE FROM disciplina WHERE nome LIKE 'Planejamento Fase 46A%'",
                "DELETE FROM turma WHERE codigo LIKE 'PLAN46A-%'",
                "DELETE FROM periodo_letivo WHERE nome LIKE 'PLAN46A-%'"
        },
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class PlanejamentoBimestralControllerIntegrationTest {

    private static final UUID SERIE_PADRAO_ID = UUID.fromString("00000000-0000-0000-0000-000000000100");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @WithMockUser
    void deveCriarAtualizarPlanejamentoEAdicionarPrevisoes() throws Exception {
        UUID periodoLetivoId = criarPeriodo("PLAN46A-2046.1", "2046-02-01", "2046-06-30");
        UUID periodoAvaliativoId = criarPeriodoAvaliativo(periodoLetivoId);
        UUID turmaId = criarTurma("PLAN46A-A", "Planejamento Fase 46A Turma A", 30, periodoLetivoId);
        UUID disciplinaId = criarDisciplina("Planejamento Fase 46A Ciencias", 80);
        UUID turmaDisciplinaId = vincularDisciplina(turmaId, disciplinaId, 80);
        UUID funcionarioId = criarFuncionario("Professor Planejamento Fase 46A", "planejamento.fase46a.professor@example.com");
        UUID professorId = criarProfessor(funcionarioId);
        UUID alocacaoId = vincularProfessorTurmaDisciplina(professorId, turmaDisciplinaId);

        String planejamentoRequest = """
                {
                  "professorTurmaDisciplinaId": "%s",
                  "periodoAvaliativoId": "%s",
                  "titulo": "Planejamento do primeiro bimestre",
                  "temaPrincipal": "Ecossistemas",
                  "descricaoInicial": "Plano inicial para aulas de ciências",
                  "objetivoGeral": "Compreender relações ecológicas",
                  "observacaoProfessor": "Turma com boa participação",
                  "reutilizavel": true,
                  "criadoComAuxilioIA": false
                }
                """.formatted(alocacaoId, periodoAvaliativoId);

        String planejamentoResponse = mockMvc.perform(post("/api/planejamentos-bimestrais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(planejamentoRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.professorTurmaDisciplinaId").value(alocacaoId.toString()))
                .andExpect(jsonPath("$.professorId").value(professorId.toString()))
                .andExpect(jsonPath("$.turmaId").value(turmaId.toString()))
                .andExpect(jsonPath("$.disciplinaId").value(disciplinaId.toString()))
                .andExpect(jsonPath("$.periodoAvaliativoId").value(periodoAvaliativoId.toString()))
                .andExpect(jsonPath("$.status").value("RASCUNHO"))
                .andExpect(jsonPath("$.aprovadoPeloProfessor").value(false))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID planejamentoId = UUID.fromString(objectMapper.readTree(planejamentoResponse).get("id").asText());

        mockMvc.perform(get("/api/planejamentos-bimestrais")
                        .param("professorId", professorId.toString())
                        .param("turmaId", turmaId.toString())
                        .param("disciplinaId", disciplinaId.toString())
                        .param("periodoAvaliativoId", periodoAvaliativoId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(planejamentoId.toString()))
                .andExpect(jsonPath("$[0].temaPrincipal").value("Ecossistemas"));

        String planejamentoAtualizadoRequest = """
                {
                  "professorTurmaDisciplinaId": "%s",
                  "periodoAvaliativoId": "%s",
                  "titulo": "Planejamento revisado",
                  "temaPrincipal": "Ecossistemas e sustentabilidade",
                  "descricaoInicial": "Plano revisado para aulas de ciências",
                  "objetivoGeral": "Relacionar ecologia e sustentabilidade",
                  "observacaoProfessor": "Revisão do professor",
                  "conteudoFinalAprovado": "Conteúdo consolidado para o bimestre",
                  "reutilizavel": true,
                  "criadoComAuxilioIA": false
                }
                """.formatted(alocacaoId, periodoAvaliativoId);

        mockMvc.perform(put("/api/planejamentos-bimestrais/{id}", planejamentoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(planejamentoAtualizadoRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Planejamento revisado"))
                .andExpect(jsonPath("$.temaPrincipal").value("Ecossistemas e sustentabilidade"));

        String aulaRequest = """
                {
                  "numeroAula": 1,
                  "temaAula": "Cadeias alimentares",
                  "objetivoAula": "Identificar produtores e consumidores",
                  "conteudoPrevisto": "Relações alimentares",
                  "metodologia": "Aula dialogada",
                  "recursos": "Quadro e projetor",
                  "atividadePrevista": "Mapa conceitual"
                }
                """;

        mockMvc.perform(post("/api/planejamentos-bimestrais/{id}/aulas-previstas", planejamentoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(aulaRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.planejamentoBimestralId").value(planejamentoId.toString()))
                .andExpect(jsonPath("$.numeroAula").value(1))
                .andExpect(jsonPath("$.temaAula").value("Cadeias alimentares"));

        mockMvc.perform(post("/api/planejamentos-bimestrais/{id}/aulas-previstas", planejamentoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(aulaRequest))
                .andExpect(status().isConflict());

        String avaliacaoRequest = """
                {
                  "titulo": "Prova de ecologia",
                  "descricao": "Avaliação prevista do bimestre",
                  "dataPrevista": "2046-04-10",
                  "peso": 1.00,
                  "valorMaximo": 10.00,
                  "tipoAvaliacao": "PROVA",
                  "conteudoCobrado": "Cadeias alimentares",
                  "orientacaoAplicacao": "Individual"
                }
                """;

        mockMvc.perform(post("/api/planejamentos-bimestrais/{id}/avaliacoes-previstas", planejamentoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(avaliacaoRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.planejamentoBimestralId").value(planejamentoId.toString()))
                .andExpect(jsonPath("$.titulo").value("Prova de ecologia"))
                .andExpect(jsonPath("$.tipoAvaliacao").value("PROVA"));

        String statusRequest = """
                {
                  "status": "APROVADO"
                }
                """;

        mockMvc.perform(patch("/api/planejamentos-bimestrais/{id}/status", planejamentoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADO"))
                .andExpect(jsonPath("$.aprovadoPeloProfessor").value(true))
                .andExpect(jsonPath("$.aulasPrevistas[0].temaAula").value("Cadeias alimentares"))
                .andExpect(jsonPath("$.avaliacoesPrevistas[0].titulo").value("Prova de ecologia"));
    }

    @Test
    @WithMockUser
    void deveRetornarNaoEncontradoParaPlanejamentoInexistente() throws Exception {
        mockMvc.perform(get("/api/planejamentos-bimestrais/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
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
                """, cargoId, "PLAN-FASE46A-" + System.nanoTime(), "Professor");

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

    private UUID criarPeriodoAvaliativo(UUID periodoLetivoId) {
        UUID periodoAvaliativoId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO periodo_avaliativo (
                    id_periodo_avaliativo, id_periodo_letivo, numero, nome,
                    data_inicio, data_fim, ativo, created_at
                )
                VALUES (?, ?, 6, 'Sexto período avaliativo', DATE '2046-02-01', DATE '2046-06-30', true, CURRENT_TIMESTAMP)
                """, periodoAvaliativoId, periodoLetivoId);
        return periodoAvaliativoId;
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
                  "registroProfissional": "RP-PLAN-46A",
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
                  "dataInicio": "2046-02-01"
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

    private String cpfAleatorio() {
        long cpf = System.nanoTime() % 1_000_000_00000L;
        return String.format("%011d", cpf);
    }
}
