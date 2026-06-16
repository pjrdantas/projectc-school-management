package br.com.escola.ia.adapter.in.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
                "MERGE INTO status_planejamento (id_status_planejamento, codigo, descricao) KEY(codigo) VALUES ('00000000-0000-0000-0000-000000000401', 'RASCUNHO', 'Rascunho')",
                "MERGE INTO tipo_conteudo_ia (id_tipo_conteudo_ia, codigo, descricao) KEY(codigo) VALUES ('00000000-0000-0000-0000-000000000501', 'PLANO_BIMESTRAL', 'Plano bimestral')",
                "MERGE INTO status_conteudo_ia (id_status_conteudo_ia, codigo, descricao) KEY(codigo) VALUES ('00000000-0000-0000-0000-000000000601', 'GERADO', 'Gerado')",
                "MERGE INTO status_conteudo_ia (id_status_conteudo_ia, codigo, descricao) KEY(codigo) VALUES ('00000000-0000-0000-0000-000000000602', 'EM_EDICAO', 'Em edição')",
                "MERGE INTO status_conteudo_ia (id_status_conteudo_ia, codigo, descricao) KEY(codigo) VALUES ('00000000-0000-0000-0000-000000000603', 'APROVADO', 'Aprovado')",
                "DELETE FROM biblioteca_conteudo_pedagogico",
                "DELETE FROM planejamento_ia_conteudo_versao",
                "DELETE FROM planejamento_ia_conteudo_gerado",
                "DELETE FROM planejamento_ia_interacao",
                "DELETE FROM planejamento_bimestral_avaliacao",
                "DELETE FROM planejamento_bimestral_aula",
                "DELETE FROM planejamento_bimestral",
                "DELETE FROM periodo_avaliativo WHERE id_periodo_letivo IN (SELECT id_periodo_letivo FROM periodo_letivo WHERE nome LIKE 'PLAN46C-%')",
                "DELETE FROM professor_turma_disciplina",
                "DELETE FROM professor",
                "DELETE FROM funcionario WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'planejamento.fase46c.%')",
                "DELETE FROM cargo WHERE codigo LIKE 'PLAN-FASE46C-%'",
                "DELETE FROM pessoa_tipo_pessoa WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'planejamento.fase46c.%')",
                "DELETE FROM pessoa WHERE email LIKE 'planejamento.fase46c.%'",
                "DELETE FROM turma_disciplina WHERE id_turma IN (SELECT id_turma FROM turma WHERE codigo LIKE 'PLAN46C-%')",
                "DELETE FROM disciplina WHERE nome LIKE 'Planejamento Fase 46C%'",
                "DELETE FROM turma WHERE codigo LIKE 'PLAN46C-%'",
                "DELETE FROM periodo_letivo WHERE nome LIKE 'PLAN46C-%'"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
        statements = {
                "DELETE FROM biblioteca_conteudo_pedagogico",
                "DELETE FROM planejamento_ia_conteudo_versao",
                "DELETE FROM planejamento_ia_conteudo_gerado",
                "DELETE FROM planejamento_ia_interacao",
                "DELETE FROM planejamento_bimestral_avaliacao",
                "DELETE FROM planejamento_bimestral_aula",
                "DELETE FROM planejamento_bimestral",
                "DELETE FROM periodo_avaliativo WHERE id_periodo_letivo IN (SELECT id_periodo_letivo FROM periodo_letivo WHERE nome LIKE 'PLAN46C-%')",
                "DELETE FROM professor_turma_disciplina",
                "DELETE FROM professor",
                "DELETE FROM funcionario WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'planejamento.fase46c.%')",
                "DELETE FROM cargo WHERE codigo LIKE 'PLAN-FASE46C-%'",
                "DELETE FROM pessoa_tipo_pessoa WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'planejamento.fase46c.%')",
                "DELETE FROM pessoa WHERE email LIKE 'planejamento.fase46c.%'",
                "DELETE FROM turma_disciplina WHERE id_turma IN (SELECT id_turma FROM turma WHERE codigo LIKE 'PLAN46C-%')",
                "DELETE FROM disciplina WHERE nome LIKE 'Planejamento Fase 46C%'",
                "DELETE FROM turma WHERE codigo LIKE 'PLAN46C-%'",
                "DELETE FROM periodo_letivo WHERE nome LIKE 'PLAN46C-%'"
        },
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class PlanejamentoIAControllerIntegrationTest {

    private static final UUID SERIE_PADRAO_ID = UUID.fromString("00000000-0000-0000-0000-000000000100");
    private static final String ESCOLA_PADRAO_ID = "00000000-0000-0000-0000-000000000047";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @WithMockUser
    void deveGerarVersionarAprovarEPublicarConteudoPedagogico() throws Exception {
        ContextoPlanejamento contexto = criarContextoPlanejamento();
        UUID planejamentoId = criarPlanejamento(contexto);

        String gerarRequest = """
                {
                  "promptProfessor": "Gerar proposta com sequência didática e atividades práticas.",
                  "tipoConteudo": " plano_bimestral ",
                  "titulo": "Sequência sobre ecossistemas",
                  "reutilizavel": true
                }
                """;

        String gerarResponse = mockMvc.perform(post("/api/planejamentos-bimestrais/{id}/ia/conteudos", planejamentoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gerarRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.planejamentoBimestralId").value(planejamentoId.toString()))
                .andExpect(jsonPath("$.escolaId").value(ESCOLA_PADRAO_ID))
                .andExpect(jsonPath("$.titulo").value("Sequência sobre ecossistemas"))
                .andExpect(jsonPath("$.tipoConteudo").value("PLANO_BIMESTRAL"))
                .andExpect(jsonPath("$.status").value("GERADO"))
                .andExpect(jsonPath("$.versao").value(1))
                .andExpect(jsonPath("$.aprovadoPeloProfessor").value(false))
                .andExpect(jsonPath("$.reutilizavel").value(true))
                .andExpect(jsonPath("$.conteudo").value(org.hamcrest.Matchers.containsString("Sugestão pedagógica simulada")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID conteudoId = UUID.fromString(objectMapper.readTree(gerarResponse).get("id").asText());

        mockMvc.perform(get("/api/planejamentos-bimestrais/{id}/ia/interacoes", planejamentoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].escolaId").value(ESCOLA_PADRAO_ID))
                .andExpect(jsonPath("$[0].promptProfessor").value("Gerar proposta com sequência didática e atividades práticas."))
                .andExpect(jsonPath("$[0].modeloIA").value("simulado-local-v1"));

        mockMvc.perform(get("/api/ia/conteudos/{id}/versoes", conteudoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numeroVersao").value(1));

        String conteudoEditado = "Conteúdo revisado sobre ecossistemas com roteiro de aula e atividade investigativa.";
        String versaoRequest = """
                {
                  "conteudo": "%s",
                  "motivoAlteracao": "Ajuste do professor para a turma."
                }
                """.formatted(conteudoEditado);

        mockMvc.perform(post("/api/ia/conteudos/{id}/versoes", conteudoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(versaoRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.conteudoGeradoId").value(conteudoId.toString()))
                .andExpect(jsonPath("$.numeroVersao").value(2))
                .andExpect(jsonPath("$.conteudo").value(conteudoEditado));

        String aprovarRequest = """
                {
                  "numeroVersao": 2,
                  "publicarBiblioteca": true
                }
                """;

        mockMvc.perform(patch("/api/ia/conteudos/{id}/aprovar-versao", conteudoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(aprovarRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(conteudoId.toString()))
                .andExpect(jsonPath("$.escolaId").value(ESCOLA_PADRAO_ID))
                .andExpect(jsonPath("$.status").value("APROVADO"))
                .andExpect(jsonPath("$.versao").value(2))
                .andExpect(jsonPath("$.aprovadoPeloProfessor").value(true))
                .andExpect(jsonPath("$.conteudo").value(conteudoEditado));

        mockMvc.perform(get("/api/biblioteca-conteudos-pedagogicos")
                        .param("tema", "Ecossistemas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].escolaId").value(ESCOLA_PADRAO_ID))
                .andExpect(jsonPath("$[0].professorId").value(contexto.professorId().toString()))
                .andExpect(jsonPath("$[0].disciplinaId").value(contexto.disciplinaId().toString()))
                .andExpect(jsonPath("$[0].tipoConteudo").value("PLANO_BIMESTRAL"))
                .andExpect(jsonPath("$[0].origem").value("PLANEJAMENTO_IA"))
                .andExpect(jsonPath("$[0].conteudo").value(conteudoEditado));

        mockMvc.perform(get("/api/biblioteca-conteudos-pedagogicos")
                        .param("professorId", contexto.professorId().toString())
                        .param("disciplinaId", contexto.disciplinaId().toString())
                        .param("tipoConteudo", " plano_bimestral ")
                        .param("tema", " ecossistemas "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].tipoConteudo").value("PLANO_BIMESTRAL"))
                .andExpect(jsonPath("$[0].conteudo").value(conteudoEditado));

        mockMvc.perform(get("/api/biblioteca-conteudos-pedagogicos")
                        .param("disciplinaId", UUID.randomUUID().toString())
                        .param("tipoConteudo", "PLANO_BIMESTRAL")
                        .param("tema", "Ecossistemas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));

        mockMvc.perform(get("/api/biblioteca-conteudos-pedagogicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].escolaId").value(ESCOLA_PADRAO_ID))
                .andExpect(jsonPath("$[0].conteudo").value(conteudoEditado));

        mockMvc.perform(post("/api/ia/conteudos/{id}/publicar-biblioteca", conteudoId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.conteudo").value(conteudoEditado));

        mockMvc.perform(get("/api/biblioteca-conteudos-pedagogicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].conteudo").value(conteudoEditado));
    }

    @Test
    @WithMockUser
    void deveBloquearPublicacaoDeConteudoNaoAprovado() throws Exception {
        UUID planejamentoId = criarPlanejamento(criarContextoPlanejamento());
        String gerarRequest = """
                {
                  "promptProfessor": "Gerar proposta inicial.",
                  "tipoConteudo": "PLANO_BIMESTRAL",
                  "titulo": "Conteúdo pendente",
                  "reutilizavel": true
                }
                """;

        String gerarResponse = mockMvc.perform(post("/api/planejamentos-bimestrais/{id}/ia/conteudos", planejamentoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gerarRequest))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID conteudoId = UUID.fromString(objectMapper.readTree(gerarResponse).get("id").asText());

        mockMvc.perform(post("/api/ia/conteudos/{id}/publicar-biblioteca", conteudoId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BUSINESS_RULE_VIOLATION"));
    }

    @Test
    @WithMockUser
    void deveBloquearFiltroBibliotecaComTipoConteudoInexistente() throws Exception {
        UUID planejamentoId = criarPlanejamento(criarContextoPlanejamento());
        String gerarRequest = """
                {
                  "promptProfessor": "Gerar conteúdo para biblioteca.",
                  "tipoConteudo": "PLANO_BIMESTRAL",
                  "titulo": "Conteúdo publicado",
                  "reutilizavel": true
                }
                """;

        String gerarResponse = mockMvc.perform(post("/api/planejamentos-bimestrais/{id}/ia/conteudos", planejamentoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gerarRequest))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID conteudoId = UUID.fromString(objectMapper.readTree(gerarResponse).get("id").asText());
        String aprovarRequest = """
                {
                  "numeroVersao": 1,
                  "publicarBiblioteca": true
                }
                """;

        mockMvc.perform(patch("/api/ia/conteudos/{id}/aprovar-versao", conteudoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(aprovarRequest))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/biblioteca-conteudos-pedagogicos")
                        .param("tipoConteudo", " roteiro_aula "))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Tipo de conteúdo de IA não encontrado: ROTEIRO_AULA."));
    }

    @Test
    @WithMockUser
    void deveValidarEntradasInvalidasDeIAAntesDeExecutarFluxo() throws Exception {
        String gerarRequestInvalido = """
                {
                  "promptProfessor": "   ",
                  "tipoConteudo": "%s",
                  "titulo": "%s",
                  "reutilizavel": true
                }
                """.formatted("A".repeat(61), "T".repeat(181));

        mockMvc.perform(post("/api/planejamentos-bimestrais/{id}/ia/conteudos", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gerarRequestInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fields[*].field", org.hamcrest.Matchers.hasItems(
                        "promptProfessor",
                        "tipoConteudo",
                        "titulo")));

        String versaoRequestInvalido = """
                {
                  "conteudo": "   ",
                  "motivoAlteracao": "%s"
                }
                """.formatted("M".repeat(2001));

        mockMvc.perform(post("/api/ia/conteudos/{id}/versoes", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(versaoRequestInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fields[*].field", org.hamcrest.Matchers.hasItems(
                        "conteudo",
                        "motivoAlteracao")));
    }

    @Test
    @WithMockUser
    void deveBloquearGeracaoComTipoConteudoInexistenteSemPersistirIA() throws Exception {
        UUID planejamentoId = criarPlanejamento(criarContextoPlanejamento());
        String gerarRequest = """
                {
                  "promptProfessor": "Gerar proposta inicial.",
                  "tipoConteudo": " roteiro_aula ",
                  "titulo": "Tipo inexistente",
                  "reutilizavel": true
                }
                """;

        mockMvc.perform(post("/api/planejamentos-bimestrais/{id}/ia/conteudos", planejamentoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gerarRequest))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Tipo de conteúdo de IA não encontrado: ROTEIRO_AULA."));

        Integer interacoes = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM planejamento_ia_interacao
                WHERE id_planejamento_bimestral = ?
                """, Integer.class, planejamentoId);
        Integer conteudos = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM planejamento_ia_conteudo_gerado
                WHERE id_planejamento_bimestral = ?
                """, Integer.class, planejamentoId);

        assertEquals(0, interacoes);
        assertEquals(0, conteudos);
    }

    @Test
    @WithMockUser
    void deveBloquearVersionamentoAprovacaoEPublicacaoDeConteudoInativo() throws Exception {
        UUID planejamentoId = criarPlanejamento(criarContextoPlanejamento());
        String gerarRequest = """
                {
                  "promptProfessor": "Gerar proposta que sera inativada.",
                  "tipoConteudo": "PLANO_BIMESTRAL",
                  "titulo": "Conteúdo inativo",
                  "reutilizavel": true
                }
                """;

        String gerarResponse = mockMvc.perform(post("/api/planejamentos-bimestrais/{id}/ia/conteudos", planejamentoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gerarRequest))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID conteudoId = UUID.fromString(objectMapper.readTree(gerarResponse).get("id").asText());
        jdbcTemplate.update("""
                UPDATE planejamento_ia_conteudo_gerado
                SET ativo = false
                WHERE id_planejamento_ia_conteudo_gerado = ?
                """, conteudoId);

        String versaoRequest = """
                {
                  "conteudo": "Tentativa de versionar conteúdo inativo.",
                  "motivoAlteracao": "Validar bloqueio."
                }
                """;
        mockMvc.perform(post("/api/ia/conteudos/{id}/versoes", conteudoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(versaoRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BUSINESS_RULE_VIOLATION"));

        String aprovarRequest = """
                {
                  "numeroVersao": 1,
                  "publicarBiblioteca": true
                }
                """;
        mockMvc.perform(patch("/api/ia/conteudos/{id}/aprovar-versao", conteudoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(aprovarRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BUSINESS_RULE_VIOLATION"));

        mockMvc.perform(post("/api/ia/conteudos/{id}/publicar-biblioteca", conteudoId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BUSINESS_RULE_VIOLATION"));
    }

    private ContextoPlanejamento criarContextoPlanejamento() throws Exception {
        UUID periodoLetivoId = criarPeriodo("PLAN46C-2046.1", "2046-02-01", "2046-06-30");
        UUID periodoAvaliativoId = criarPeriodoAvaliativo(periodoLetivoId);
        UUID turmaId = criarTurma("PLAN46C-A", "Planejamento Fase 46C Turma A", 30, periodoLetivoId);
        UUID disciplinaId = criarDisciplina("Planejamento Fase 46C Ciencias", 80);
        UUID turmaDisciplinaId = vincularDisciplina(turmaId, disciplinaId, 80);
        UUID funcionarioId = criarFuncionario("Professor Planejamento Fase 46C", "planejamento.fase46c.professor@example.com");
        UUID professorId = criarProfessor(funcionarioId);
        UUID alocacaoId = vincularProfessorTurmaDisciplina(professorId, turmaDisciplinaId);
        return new ContextoPlanejamento(periodoAvaliativoId, professorId, disciplinaId, alocacaoId);
    }

    private UUID criarPlanejamento(ContextoPlanejamento contexto) throws Exception {
        String planejamentoRequest = """
                {
                  "professorTurmaDisciplinaId": "%s",
                  "periodoAvaliativoId": "%s",
                  "titulo": "Planejamento para IA",
                  "temaPrincipal": "Ecossistemas",
                  "descricaoInicial": "Plano inicial para aulas de ciências",
                  "objetivoGeral": "Compreender relações ecológicas",
                  "observacaoProfessor": "Turma com boa participação",
                  "reutilizavel": true,
                  "criadoComAuxilioIA": true
                }
                """.formatted(contexto.alocacaoId(), contexto.periodoAvaliativoId());

        String response = mockMvc.perform(post("/api/planejamentos-bimestrais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(planejamentoRequest))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(objectMapper.readTree(response).get("id").asText());
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
                """, cargoId, "PLAN-FASE46C-" + System.nanoTime(), "Professor");

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
                VALUES (?, ?, 7, 'Sétimo período avaliativo', DATE '2046-02-01', DATE '2046-06-30', true, CURRENT_TIMESTAMP)
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
                  "registroProfissional": "RP-PLAN-46C-%s",
                  "formacao": "Licenciatura"
                }
                """.formatted(funcionarioId, System.nanoTime());

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

    private record ContextoPlanejamento(
            UUID periodoAvaliativoId,
            UUID professorId,
            UUID disciplinaId,
            UUID alocacaoId) {
    }
}
