package br.com.escola.professor.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
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
                "MERGE INTO status_planejamento (id_status_planejamento, codigo, descricao) KEY(codigo) VALUES ('00000000-0000-0000-0000-000000000401', 'RASCUNHO', 'Rascunho')",
                "MERGE INTO status_planejamento (id_status_planejamento, codigo, descricao) KEY(codigo) VALUES ('00000000-0000-0000-0000-000000000403', 'APROVADO', 'Aprovado')",
                "MERGE INTO tipo_avaliacao (id_tipo_avaliacao, codigo, descricao) KEY(codigo) VALUES ('00000000-0000-0000-0000-000000000301', 'PROVA', 'Prova')",
                "DELETE FROM frequencia_aluno",
                "DELETE FROM frequencia_professor",
                "DELETE FROM aula",
                "DELETE FROM diario_classe_lancamento",
                "DELETE FROM avaliacao",
                "DELETE FROM planejamento_bimestral_avaliacao",
                "DELETE FROM planejamento_bimestral_aula",
                "DELETE FROM planejamento_bimestral",
                "DELETE FROM periodo_avaliativo WHERE id_periodo_letivo IN (SELECT id_periodo_letivo FROM periodo_letivo WHERE nome LIKE 'DIARIO57-%')",
                "DELETE FROM professor_turma_disciplina",
                "DELETE FROM professor",
                "DELETE FROM funcionario WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'diario.fase57.%')",
                "DELETE FROM cargo WHERE codigo LIKE 'DIARIO-FASE57-%'",
                "DELETE FROM matricula_etapa",
                "DELETE FROM matricula WHERE id_aluno IN (SELECT id_aluno FROM aluno WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'diario.fase57.%'))",
                "DELETE FROM aluno WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'diario.fase57.%')",
                "DELETE FROM pessoa_tipo_pessoa WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'diario.fase57.%')",
                "DELETE FROM pessoa WHERE email LIKE 'diario.fase57.%'",
                "DELETE FROM turma_disciplina WHERE id_turma IN (SELECT id_turma FROM turma WHERE codigo LIKE 'DIARIO57-%')",
                "DELETE FROM disciplina WHERE nome LIKE 'Diario Fase 57%'",
                "DELETE FROM turma WHERE codigo LIKE 'DIARIO57-%'",
                "DELETE FROM periodo_letivo WHERE nome LIKE 'DIARIO57-%'"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
        statements = {
                "DELETE FROM frequencia_aluno",
                "DELETE FROM frequencia_professor",
                "DELETE FROM aula",
                "DELETE FROM diario_classe_lancamento",
                "DELETE FROM avaliacao",
                "DELETE FROM planejamento_bimestral_avaliacao",
                "DELETE FROM planejamento_bimestral_aula",
                "DELETE FROM planejamento_bimestral",
                "DELETE FROM periodo_avaliativo WHERE id_periodo_letivo IN (SELECT id_periodo_letivo FROM periodo_letivo WHERE nome LIKE 'DIARIO57-%')",
                "DELETE FROM professor_turma_disciplina",
                "DELETE FROM professor",
                "DELETE FROM funcionario WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'diario.fase57.%')",
                "DELETE FROM cargo WHERE codigo LIKE 'DIARIO-FASE57-%'",
                "DELETE FROM matricula_etapa",
                "DELETE FROM matricula WHERE id_aluno IN (SELECT id_aluno FROM aluno WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'diario.fase57.%'))",
                "DELETE FROM aluno WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'diario.fase57.%')",
                "DELETE FROM pessoa_tipo_pessoa WHERE id_pessoa IN (SELECT id_pessoa FROM pessoa WHERE email LIKE 'diario.fase57.%')",
                "DELETE FROM pessoa WHERE email LIKE 'diario.fase57.%'",
                "DELETE FROM turma_disciplina WHERE id_turma IN (SELECT id_turma FROM turma WHERE codigo LIKE 'DIARIO57-%')",
                "DELETE FROM disciplina WHERE nome LIKE 'Diario Fase 57%'",
                "DELETE FROM turma WHERE codigo LIKE 'DIARIO57-%'",
                "DELETE FROM periodo_letivo WHERE nome LIKE 'DIARIO57-%'"
        },
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class DiarioClasseControllerIntegrationTest {

    private static final UUID SERIE_PADRAO_ID = UUID.fromString("00000000-0000-0000-0000-000000000100");
    private static final LocalDate HOJE_FIXO = LocalDate.of(2026, 7, 1);
    private static final ZoneId ZONE_ID = ZoneId.of("America/Sao_Paulo");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @TestConfiguration
    static class DiarioClasseClockTestConfig {

        @Bean
        @Primary
        Clock diarioClasseClock() {
            return Clock.fixed(HOJE_FIXO.atStartOfDay(ZONE_ID).toInstant(), ZONE_ID);
        }
    }

    @Test
    @WithMockUser
    void deveCarregarDiarioClasseMensalConsolidado() throws Exception {
        UUID periodoId = criarPeriodo("DIARIO57-2058.1", "2058-02-01", "2058-06-30");
        UUID periodoAvaliativoId = criarPeriodoAvaliativo(periodoId, "1º Bimestre", 1, "2058-04-01", "2058-06-30");
        UUID turmaId = criarTurma("DIARIO57-A", "Diario Fase 57 Turma A", 30, periodoId);
        UUID disciplinaId = criarDisciplina("Diario Fase 57 Matematica", 80);
        UUID turmaDisciplinaId = vincularDisciplina(turmaId, disciplinaId, 80);
        UUID funcionarioId = criarFuncionario("Professor Diario Fase 57", "diario.fase57.professor@example.com");
        UUID professorId = criarProfessor(funcionarioId);
        UUID alocacaoId = vincularProfessorTurmaDisciplina(professorId, turmaDisciplinaId);
        UUID alunoUmId = criarAluno("Aluno Diario Fase 57 A", "diario.fase57.aluno.a@example.com");
        UUID alunoDoisId = criarAluno("Aluno Diario Fase 57 B", "diario.fase57.aluno.b@example.com");
        UUID matriculaUmId = criarMatricula(alunoUmId, turmaId, periodoId);
        UUID matriculaDoisId = criarMatricula(alunoDoisId, turmaId, periodoId);
        atualizarStatusMatricula(matriculaUmId, "EFETIVADA", "Aluno ativo no diário");
        atualizarStatusMatricula(matriculaDoisId, "EFETIVADA", "Aluno ativo no diário");
        UUID planejamentoId = criarPlanejamento(alocacaoId, periodoAvaliativoId);
        adicionarAulaPrevista(planejamentoId);
        criarAvaliacao(alocacaoId);
        UUID aulaId = criarAula(alocacaoId, "2058-06-12");
        registrarFrequenciaAluno(aulaId, matriculaUmId, "PRESENTE");
        registrarFrequenciaAluno(aulaId, matriculaDoisId, "FALTA");

        mockMvc.perform(get("/api/diarios-classe")
                        .param("idProfessor", professorId.toString())
                        .param("idTurma", turmaId.toString())
                        .param("idDisciplina", disciplinaId.toString())
                        .param("anoLetivo", "2058")
                        .param("mes", "6")
                        .param("dataReferencia", "2058-06-26"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cabecalho.idProfessor").value(professorId.toString()))
                .andExpect(jsonPath("$.cabecalho.idTurma").value(turmaId.toString()))
                .andExpect(jsonPath("$.cabecalho.idDisciplina").value(disciplinaId.toString()))
                .andExpect(jsonPath("$.cabecalho.anoLetivo").value(2058))
                .andExpect(jsonPath("$.cabecalho.mes").value(6))
                .andExpect(jsonPath("$.alunos[0].nome").value("Aluno Diario Fase 57 A"))
                .andExpect(jsonPath("$.alunos[0].frequencias.12").value("P"))
                .andExpect(jsonPath("$.alunos[1].nome").value("Aluno Diario Fase 57 B"))
                .andExpect(jsonPath("$.alunos[1].frequencias.12").value("F"))
                .andExpect(jsonPath("$.conteudosPlanejados[0].periodo").value("Aula 1"))
                .andExpect(jsonPath("$.conteudosPlanejados[0].descricao").value("Números naturais e operações"))
                .andExpect(jsonPath("$.avaliacoes[0].descricao").value("Prova mensal de matemática"))
                .andExpect(jsonPath("$.avaliacoes[0].valor").value("0 a 10"))
                .andExpect(jsonPath("$.assinatura.nomeProfessor").value(""))
                .andExpect(jsonPath("$.bloqueado").value(false));
    }

    @Test
    @WithMockUser
    void deveRetornarNaoEncontradoQuandoNaoExistirAlocacaoDoDiario() throws Exception {
        mockMvc.perform(get("/api/diarios-classe")
                        .param("idProfessor", UUID.randomUUID().toString())
                        .param("idTurma", UUID.randomUUID().toString())
                        .param("idDisciplina", UUID.randomUUID().toString())
                        .param("anoLetivo", "2058")
                        .param("mes", "6")
                        .param("dataReferencia", "2058-06-26"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void deveSalvarLancamentoControladoDoDiarioClasse() throws Exception {
        DiarioLancamentoFixture fixture = criarCenarioLancamento(HOJE_FIXO, "WRITE");
        LocalDate dataLancamento = fixture.dataLancamento();
        String idDiarioClasse = fixture.idDiarioClasse();
        String requestBody = requestLancamento(fixture);

        mockMvc.perform(put("/api/diarios-classe/{idDiarioClasse}", idDiarioClasse)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idDiarioClasse").value(idDiarioClasse))
                .andExpect(jsonPath("$.status").value("SALVO"))
                .andExpect(jsonPath("$.bloqueado").value(true));

        mockMvc.perform(get("/api/diarios-classe")
                        .param("idProfessor", fixture.professorId().toString())
                        .param("idTurma", fixture.turmaId().toString())
                        .param("idDisciplina", fixture.disciplinaId().toString())
                        .param("anoLetivo", String.valueOf(dataLancamento.getYear()))
                        .param("mes", String.valueOf(dataLancamento.getMonthValue()))
                        .param("dataReferencia", dataLancamento.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alunos[0].frequencias.%d".formatted(dataLancamento.getDayOfMonth())).value("P"))
                .andExpect(jsonPath("$.alunos[1].frequencias.%d".formatted(dataLancamento.getDayOfMonth())).value("F"))
                .andExpect(jsonPath("$.observacoes[0]").value("Registro inicial do diario"))
                .andExpect(jsonPath("$.assinatura.nomeProfessor").value("Professor Diario Fase 57 Write"))
                .andExpect(jsonPath("$.assinatura.dataAssinatura").value(dataLancamento.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))))
                .andExpect(jsonPath("$.bloqueado").value(true));

        mockMvc.perform(put("/api/diarios-classe/{idDiarioClasse}", idDiarioClasse)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void deveRejeitarLancamentoForaDoDiaCorrente() throws Exception {
        DiarioLancamentoFixture fixture = criarCenarioLancamento(HOJE_FIXO.plusDays(1), "FUTURO");

        mockMvc.perform(put("/api/diarios-classe/{idDiarioClasse}", fixture.idDiarioClasse())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestLancamento(fixture)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deveRejeitarLancamentoEmFimDeSemana() throws Exception {
        DiarioLancamentoFixture fixture = criarCenarioLancamento(LocalDate.of(2026, 7, 4), "SABADO");

        mockMvc.perform(put("/api/diarios-classe/{idDiarioClasse}", fixture.idDiarioClasse())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestLancamento(fixture)))
                .andExpect(status().isBadRequest());
    }

    private DiarioLancamentoFixture criarCenarioLancamento(LocalDate dataLancamento, String sufixo) throws Exception {
        UUID periodoId = criarPeriodo(
                "DIARIO57-WRITE-" + sufixo + "-" + System.nanoTime(),
                dataLancamento.withDayOfMonth(1).toString(),
                dataLancamento.withDayOfMonth(dataLancamento.lengthOfMonth()).toString());
        UUID turmaId = criarTurma("DIARIO57-W" + sufixo + Math.abs(System.nanoTime() % 10000), "Diario Fase 57 Turma " + sufixo, 30, periodoId);
        UUID disciplinaId = criarDisciplina("Diario Fase 57 Escrita " + sufixo + " " + System.nanoTime(), 80);
        UUID turmaDisciplinaId = vincularDisciplina(turmaId, disciplinaId, 80);
        UUID funcionarioId = criarFuncionario("Professor Diario Fase 57 " + sufixo, "diario.fase57.write." + sufixo.toLowerCase() + ".professor@example.com");
        UUID professorId = criarProfessor(funcionarioId);
        UUID alocacaoId = vincularProfessorTurmaDisciplina(professorId, turmaDisciplinaId);
        UUID alunoUmId = criarAluno("Aluno Diario Fase 57 " + sufixo + " A", "diario.fase57.write." + sufixo.toLowerCase() + ".a@example.com");
        UUID alunoDoisId = criarAluno("Aluno Diario Fase 57 " + sufixo + " B", "diario.fase57.write." + sufixo.toLowerCase() + ".b@example.com");
        UUID matriculaUmId = criarMatricula(alunoUmId, turmaId, periodoId);
        UUID matriculaDoisId = criarMatricula(alunoDoisId, turmaId, periodoId);
        atualizarStatusMatricula(matriculaUmId, "EFETIVADA", "Aluno ativo no diário");
        atualizarStatusMatricula(matriculaDoisId, "EFETIVADA", "Aluno ativo no diário");
        String idDiarioClasse = "diario-%d-%02d-%s".formatted(
                dataLancamento.getYear(),
                dataLancamento.getMonthValue(),
                alocacaoId);
        return new DiarioLancamentoFixture(
                dataLancamento,
                idDiarioClasse,
                professorId,
                turmaId,
                disciplinaId,
                alunoUmId,
                alunoDoisId);
    }

    private String requestLancamento(DiarioLancamentoFixture fixture) {
        LocalDate dataLancamento = fixture.dataLancamento();
        return """
                {
                  "idDiarioClasse": "%s",
                  "dataLancamento": "%s",
                  "frequencias": [
                    {
                      "idAluno": "%s",
                      "data": "%s",
                      "dia": %d,
                      "status": "P"
                    },
                    {
                      "idAluno": "%s",
                      "data": "%s",
                      "dia": %d,
                      "status": "F"
                    }
                  ],
                  "conteudos": [
                    {
                      "periodo": "Dia %d",
                      "descricao": "Conteudo registrado no diario",
                      "alterado": false
                    }
                  ],
                  "observacoes": [
                    "Registro inicial do diario"
                  ],
                  "assinatura": {
                    "nomeProfessor": "Professor Diario Fase 57 Write",
                    "dataAssinatura": "%s"
                  }
                }
                """.formatted(
                fixture.idDiarioClasse(),
                dataLancamento,
                fixture.alunoUmId(),
                dataLancamento,
                dataLancamento.getDayOfMonth(),
                fixture.alunoDoisId(),
                dataLancamento,
                dataLancamento.getDayOfMonth(),
                dataLancamento.getDayOfMonth(),
                dataLancamento);
    }

    private record DiarioLancamentoFixture(
            LocalDate dataLancamento,
            String idDiarioClasse,
            UUID professorId,
            UUID turmaId,
            UUID disciplinaId,
            UUID alunoUmId,
            UUID alunoDoisId) {
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
                """, cargoId, "DIARIO-FASE57-" + System.nanoTime(), "Professor");

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

    private UUID criarPeriodoAvaliativo(UUID periodoLetivoId, String nome, int numero, String dataInicio, String dataFim) {
        UUID periodoAvaliativoId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO periodo_avaliativo (
                    id_periodo_avaliativo, numero, nome, data_inicio, data_fim, ativo, created_at, id_periodo_letivo
                ) VALUES (?, ?, ?, ?, ?, true, CURRENT_TIMESTAMP, ?)
                """, periodoAvaliativoId, numero, nome, java.sql.Date.valueOf(dataInicio), java.sql.Date.valueOf(dataFim), periodoLetivoId);
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
                  "registroProfissional": "RP-DIARIO-57",
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
                  "dataInicio": "2058-02-01"
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

    private UUID criarPlanejamento(UUID alocacaoId, UUID periodoAvaliativoId) throws Exception {
        String requestBody = """
                {
                  "professorTurmaDisciplinaId": "%s",
                  "periodoAvaliativoId": "%s",
                  "titulo": "Planejamento diário consolidado",
                  "temaPrincipal": "Números naturais",
                  "descricaoInicial": "Plano base do bimestre",
                  "objetivoGeral": "Consolidar operações fundamentais",
                  "observacaoProfessor": "Turma com bom ritmo",
                  "reutilizavel": true,
                  "criadoComAuxilioIA": false
                }
                """.formatted(alocacaoId, periodoAvaliativoId);

        String responseBody = mockMvc.perform(post("/api/planejamentos-bimestrais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());
    }

    private void adicionarAulaPrevista(UUID planejamentoId) throws Exception {
        String requestBody = """
                {
                  "numeroAula": 1,
                  "temaAula": "Operações com naturais",
                  "objetivoAula": "Resolver somas e subtrações",
                  "conteudoPrevisto": "Números naturais e operações"
                }
                """;

        mockMvc.perform(post("/api/planejamentos-bimestrais/{id}/aulas-previstas", planejamentoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());
    }

    private void criarAvaliacao(UUID alocacaoId) throws Exception {
        String requestBody = """
                {
                  "professorTurmaDisciplinaId": "%s",
                  "titulo": "Prova mensal de matemática",
                  "descricao": "Avaliação do mês de junho",
                  "dataAplicacao": "2058-06-20",
                  "valorMaximo": 10.00,
                  "peso": 1.00,
                  "tipoAvaliacao": "PROVA"
                }
                """.formatted(alocacaoId);

        mockMvc.perform(post("/api/avaliacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());
    }

    private UUID criarAula(UUID alocacaoId, String dataAula) throws Exception {
        String requestBody = """
                {
                  "professorTurmaDisciplinaId": "%s",
                  "dataAula": "%s",
                  "realizada": true
                }
                """.formatted(alocacaoId, dataAula);

        String responseBody = mockMvc.perform(post("/api/aulas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());
    }

    private void registrarFrequenciaAluno(UUID aulaId, UUID matriculaId, String situacao) throws Exception {
        String requestBody = """
                {
                  "matriculaId": "%s",
                  "situacao": "%s"
                }
                """.formatted(matriculaId, situacao);

        mockMvc.perform(post("/api/aulas/{id}/frequencias-alunos", aulaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());
    }

    private String cpfAleatorio() {
        long numero = Math.abs(System.nanoTime()) % 1_000_000_00000L;
        return String.format("%011d", numero);
    }
}
