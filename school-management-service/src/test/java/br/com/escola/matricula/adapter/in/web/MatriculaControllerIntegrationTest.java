package br.com.escola.matricula.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(
        statements = {
                "DELETE FROM matricula_documento_entregue",
                "DELETE FROM matricula_documento_exigido",
                "DELETE FROM pessoa_documento",
                "DELETE FROM documento",
                "DELETE FROM boletim_item",
                "DELETE FROM boletim",
                "DELETE FROM matricula_etapa",
                "DELETE FROM matricula",
                "DELETE FROM disciplina WHERE nome LIKE 'MATRICULA-Conclusao-%'",
                "DELETE FROM turma WHERE codigo LIKE 'MATRICULA-%' OR codigo LIKE 'MATR-%'",
                "DELETE FROM periodo_letivo WHERE nome LIKE 'MATRICULA-%'",
                "DELETE FROM serie WHERE nome LIKE 'MATRICULA-%'"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class MatriculaControllerIntegrationTest {

    private static final UUID SERIE_PADRAO_ID = UUID.fromString("00000000-0000-0000-0000-000000000100");
    private static final UUID TIPO_MATRICULA_PRIMEIRA_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000061");
    private static final UUID TIPO_DOCUMENTO_HISTORICO_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000095");
    private static final UUID TIPO_DOCUMENTO_CPF_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000092");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @WithMockUser
    void deveCriarConsultarEAtualizarStatusDaMatriculaComEtapasIniciais() throws Exception {
        UUID alunoId = criarAluno("Aluno Matricula Fluxo", cpfAleatorio(), "aluno.matricula.fluxo@example.com");
        UUID periodoId = criarPeriodo("MATRICULA-2031.1", "2031-02-01", "2031-06-30");
        UUID turmaId = criarTurma("MATRICULA-TURMA-A", "Matricula Turma A", 30, periodoId);

        String matriculaRequest = """
                {
                  "alunoId": "%s",
                  "turmaId": "%s",
                  "periodoLetivoId": "%s",
                  "tipoMatricula": "PRIMEIRA_MATRICULA",
                  "observacao": "Matrícula criada pelo fluxo de integração"
                }
                """.formatted(alunoId, turmaId, periodoId);

        String matriculaResponse = mockMvc.perform(post("/api/matriculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(matriculaRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.alunoId").value(alunoId.toString()))
                .andExpect(jsonPath("$.turmaId").value(turmaId.toString()))
                .andExpect(jsonPath("$.periodoLetivoId").value(periodoId.toString()))
                .andExpect(jsonPath("$.tipoMatricula").value("PRIMEIRA_MATRICULA"))
                .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"))
                .andExpect(jsonPath("$.etapas[0].status").value("PENDENTE"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID matriculaId = UUID.fromString(objectMapper.readTree(matriculaResponse).get("id").asText());

        mockMvc.perform(get("/api/matriculas")
                        .param("alunoId", alunoId.toString())
                        .param("status", "EM_ANDAMENTO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(matriculaId.toString()))
                .andExpect(jsonPath("$[0].etapas[0].status").value("PENDENTE"));

        String statusRequest = """
                {
                  "status": "EFETIVADA",
                  "justificativa": "Documentos conferidos"
                }
                """;

        mockMvc.perform(patch("/api/matriculas/{id}/status", matriculaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(matriculaId.toString()))
                .andExpect(jsonPath("$.status").value("EFETIVADA"))
                .andExpect(jsonPath("$.observacao").value(org.hamcrest.Matchers.containsString("Documentos conferidos")));
    }

    @Test
    @WithMockUser
    void deveConcluirEtapaERegistrarDocumentoEntregueDaMatricula() throws Exception {
        UUID alunoId = criarAluno("Aluno Matricula Documento", cpfAleatorio(), "aluno.matricula.documento@example.com");
        UUID periodoId = criarPeriodo("MATRICULA-2032.1", "2032-02-01", "2032-06-30");
        UUID turmaId = criarTurma("MATRICULA-TURMA-C", "Matricula Turma C", 30, periodoId);
        criarDocumentoExigidoPrimeiraMatricula("00000000-0000-0000-0000-000000000095", true, 1);
        UUID matriculaId = criarMatricula(alunoId, turmaId, periodoId);
        atualizarStatusMatricula(matriculaId, "AGUARDANDO_DOCUMENTOS", "Aguardando documento obrigatório");

        String etapasResponse = mockMvc.perform(get("/api/matriculas/{id}/etapas", matriculaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDENTE"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID etapaId = UUID.fromString(objectMapper.readTree(etapasResponse).get(0).get("id").asText());

        String etapaRequest = """
                {
                  "status": "CONCLUIDA",
                  "observacao": "Documentação inicial validada"
                }
                """;

        mockMvc.perform(patch("/api/matriculas/{id}/etapas/{etapaId}/status", matriculaId, etapaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(etapaRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(etapaId.toString()))
                .andExpect(jsonPath("$.status").value("CONCLUIDA"))
                .andExpect(jsonPath("$.dataConclusao").isNotEmpty())
                .andExpect(jsonPath("$.observacao").value("Documentação inicial validada"));

        mockMvc.perform(get("/api/matriculas/{id}/documentos-exigidos", matriculaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipoDocumento").value("HISTORICO_ESCOLAR"))
                .andExpect(jsonPath("$[0].obrigatorio").value(true))
                .andExpect(jsonPath("$[0].entregue").value(false));

        UUID documentoId = criarDocumentoAluno(alunoId, "historico-matricula-2032.pdf");
        String documentoRequest = """
                {
                  "documentoId": "%s",
                  "conferido": true,
                  "observacao": "Documento conferido na matrícula"
                }
                """.formatted(documentoId);

        mockMvc.perform(post("/api/matriculas/{id}/documentos-entregues", matriculaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(documentoRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.matriculaId").value(matriculaId.toString()))
                .andExpect(jsonPath("$.documentoId").value(documentoId.toString()))
                .andExpect(jsonPath("$.nomeArquivo").value("historico-matricula-2032.pdf"))
                .andExpect(jsonPath("$.conferido").value(true))
                .andExpect(jsonPath("$.dataConferencia").isNotEmpty());

        mockMvc.perform(get("/api/matriculas/{id}/documentos-entregues", matriculaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].matriculaId").value(matriculaId.toString()))
                .andExpect(jsonPath("$[0].documentoId").value(documentoId.toString()))
                .andExpect(jsonPath("$[0].observacao").value("Documento conferido na matrícula"));

        mockMvc.perform(get("/api/matriculas/{id}/documentos-exigidos", matriculaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipoDocumento").value("HISTORICO_ESCOLAR"))
                .andExpect(jsonPath("$[0].entregue").value(true))
                .andExpect(jsonPath("$[0].documentoId").value(documentoId.toString()));

        mockMvc.perform(get("/api/matriculas")
                        .param("alunoId", alunoId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(matriculaId.toString()))
                .andExpect(jsonPath("$[0].status").value("EM_ANDAMENTO"));
    }

    @Test
    @WithMockUser
    void deveBloquearMatriculaDuplicadaDoMesmoAlunoNoPeriodoLetivo() throws Exception {
        UUID alunoId = criarAluno("Aluno Matricula Duplicada", cpfAleatorio(), "aluno.matricula.duplicada@example.com");
        UUID periodoId = criarPeriodo("MATRICULA-2031.2", "2031-08-01", "2031-12-15");
        UUID turmaId = criarTurma("MATRICULA-TURMA-B", "Matricula Turma B", 30, periodoId);

        String request = """
                {
                  "alunoId": "%s",
                  "turmaId": "%s",
                  "periodoLetivoId": "%s",
                  "tipoMatricula": "PRIMEIRA_MATRICULA"
                }
                """.formatted(alunoId, turmaId, periodoId);

        mockMvc.perform(post("/api/matriculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/matriculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void deveCriarRematriculaQuandoMatriculaAnteriorEstiverConcluidaESeriePosterior() throws Exception {
        UUID alunoId = criarAluno("Aluno Rematricula", cpfAleatorio(), "aluno.rematricula@example.com");
        UUID segundaSerieId = criarSerie("MATRICULA-2-ANO", 2);
        UUID periodoAnteriorId = criarPeriodo("MATRICULA-2033.1", "2033-02-01", "2033-06-30");
        UUID periodoNovoId = criarPeriodo("MATRICULA-2034.1", "2034-02-01", "2034-06-30");
        UUID turmaAnteriorId = criarTurma("MATR-ANT", "Matricula Turma Anterior", 30, periodoAnteriorId);
        UUID turmaNovaId = criarTurma(
                "MATR-REN",
                "Matricula Turma Renovacao",
                30,
                periodoNovoId,
                segundaSerieId);

        UUID matriculaAnteriorId = criarMatricula(alunoId, turmaAnteriorId, periodoAnteriorId);
        atualizarStatusMatricula(matriculaAnteriorId, "CONCLUIDA", "Ano letivo concluído");

        String request = """
                {
                  "alunoId": "%s",
                  "turmaId": "%s",
                  "periodoLetivoId": "%s",
                  "tipoMatricula": "RENOVACAO",
                  "observacao": "Renovação para série posterior"
                }
                """.formatted(alunoId, turmaNovaId, periodoNovoId);

        mockMvc.perform(post("/api/matriculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.alunoId").value(alunoId.toString()))
                .andExpect(jsonPath("$.turmaId").value(turmaNovaId.toString()))
                .andExpect(jsonPath("$.periodoLetivoId").value(periodoNovoId.toString()))
                .andExpect(jsonPath("$.tipoMatricula").value("RENOVACAO"))
                .andExpect(jsonPath("$.status").value("SOLICITADA"))
                .andExpect(jsonPath("$.serieNome").value("MATRICULA-2-ANO"));
    }

    @Test
    @WithMockUser
    void deveCriarRematriculaOperacionalAPartirDaMatriculaConcluida() throws Exception {
        UUID alunoId = criarAluno("Aluno Rematricula Operacional", cpfAleatorio(), "aluno.rematricula.operacional@example.com");
        UUID segundaSerieId = criarSerie("MATRICULA-2-ANO-OPER", 2);
        UUID periodoAnteriorId = criarPeriodo("MATRICULA-2038.1", "2038-02-01", "2038-12-15");
        UUID periodoNovoId = criarPeriodo("MATRICULA-2039.1", "2039-02-01", "2039-12-15");
        UUID turmaAnteriorId = criarTurma("MATR-ANT-OP", "Matricula Turma Anterior Operacional", 30, periodoAnteriorId);
        UUID turmaNovaId = criarTurma(
                "MATR-REN-OP",
                "Matricula Turma Renovacao Operacional",
                30,
                periodoNovoId,
                segundaSerieId);
        UUID matriculaAnteriorId = criarMatricula(alunoId, turmaAnteriorId, periodoAnteriorId);
        atualizarStatusMatricula(matriculaAnteriorId, "CONCLUIDA", "Ano letivo concluído");

        String request = """
                {
                  "turmaId": "%s",
                  "periodoLetivoId": "%s",
                  "observacao": "Rematrícula gerada pelo fluxo operacional"
                }
                """.formatted(turmaNovaId, periodoNovoId);

        mockMvc.perform(post("/api/matriculas/{id}/rematricula", matriculaAnteriorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.alunoId").value(alunoId.toString()))
                .andExpect(jsonPath("$.turmaId").value(turmaNovaId.toString()))
                .andExpect(jsonPath("$.periodoLetivoId").value(periodoNovoId.toString()))
                .andExpect(jsonPath("$.tipoMatricula").value("RENOVACAO"))
                .andExpect(jsonPath("$.status").value("SOLICITADA"))
                .andExpect(jsonPath("$.observacao").value("Rematrícula gerada pelo fluxo operacional"));
    }

    @Test
    @WithMockUser
    void deveConsultarElegibilidadeDeRematriculaComDestinoValido() throws Exception {
        UUID alunoId = criarAluno("Aluno Elegivel Rematricula", cpfAleatorio(), "aluno.elegivel.rematricula@example.com");
        UUID segundaSerieId = criarSerie("MATRICULA-2-ANO-ELEG", 2);
        UUID periodoAnteriorId = criarPeriodo("MATRICULA-2042.1", "2042-02-01", "2042-12-15");
        UUID periodoNovoId = criarPeriodo("MATRICULA-2043.1", "2043-02-01", "2043-12-15");
        UUID turmaAnteriorId = criarTurma("MATR-ANT-ELEG", "Matricula Turma Anterior Elegivel", 30, periodoAnteriorId);
        UUID turmaNovaId = criarTurma(
                "MATR-REN-ELEG",
                "Matricula Turma Renovacao Elegivel",
                30,
                periodoNovoId,
                segundaSerieId);
        UUID matriculaAnteriorId = criarMatricula(alunoId, turmaAnteriorId, periodoAnteriorId);
        atualizarStatusMatricula(matriculaAnteriorId, "CONCLUIDA", "Ano letivo concluído");

        mockMvc.perform(get("/api/matriculas/{id}/rematricula/elegibilidade", matriculaAnteriorId)
                        .param("turmaId", turmaNovaId.toString())
                        .param("periodoLetivoId", periodoNovoId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matriculaBaseId").value(matriculaAnteriorId.toString()))
                .andExpect(jsonPath("$.alunoId").value(alunoId.toString()))
                .andExpect(jsonPath("$.statusBase").value("CONCLUIDA"))
                .andExpect(jsonPath("$.turmaDestinoId").value(turmaNovaId.toString()))
                .andExpect(jsonPath("$.periodoLetivoDestinoId").value(periodoNovoId.toString()))
                .andExpect(jsonPath("$.serieDestinoNome").value("MATRICULA-2-ANO-ELEG"))
                .andExpect(jsonPath("$.elegivel").value(true))
                .andExpect(jsonPath("$.motivos").isEmpty());
    }

    @Test
    @WithMockUser
    void deveBloquearRematriculaQuandoMatriculaAnteriorNaoEstiverEfetivada() throws Exception {
        UUID alunoId = criarAluno("Aluno Rematricula Bloqueada", cpfAleatorio(), "aluno.rematricula.bloqueada@example.com");
        UUID segundaSerieId = criarSerie("MATRICULA-2-ANO-BLOQ", 2);
        UUID periodoAnteriorId = criarPeriodo("MATRICULA-2035.1", "2035-02-01", "2035-06-30");
        UUID periodoNovoId = criarPeriodo("MATRICULA-2036.1", "2036-02-01", "2036-06-30");
        UUID turmaAnteriorId = criarTurma("MATR-ANT-B", "Matricula Turma Anterior Bloqueada", 30, periodoAnteriorId);
        UUID turmaNovaId = criarTurma(
                "MATR-REN-B",
                "Matricula Turma Renovacao Bloqueada",
                30,
                periodoNovoId,
                segundaSerieId);

        criarMatricula(alunoId, turmaAnteriorId, periodoAnteriorId);

        String request = """
                {
                  "alunoId": "%s",
                  "turmaId": "%s",
                  "periodoLetivoId": "%s",
                  "tipoMatricula": "RENOVACAO"
                }
                """.formatted(alunoId, turmaNovaId, periodoNovoId);

        mockMvc.perform(post("/api/matriculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Rematrícula não permitida: matrícula anterior deve estar concluída para renovação"));
    }

    @Test
    @WithMockUser
    void deveBloquearRematriculaOperacionalQuandoMatriculaBaseNaoEstiverConcluida() throws Exception {
        UUID alunoId = criarAluno("Aluno Rematricula Operacional Bloqueada", cpfAleatorio(), "aluno.rematricula.operacional.bloqueada@example.com");
        UUID segundaSerieId = criarSerie("MATRICULA-2-ANO-OPER-BLOQ", 2);
        UUID periodoAnteriorId = criarPeriodo("MATRICULA-2040.1", "2040-02-01", "2040-12-15");
        UUID periodoNovoId = criarPeriodo("MATRICULA-2041.1", "2041-02-01", "2041-12-15");
        UUID turmaAnteriorId = criarTurma("MATR-ANT-OP-B", "Matricula Turma Anterior Operacional Bloqueada", 30, periodoAnteriorId);
        UUID turmaNovaId = criarTurma(
                "MATR-REN-OP-B",
                "Matricula Turma Renovacao Operacional Bloqueada",
                30,
                periodoNovoId,
                segundaSerieId);
        UUID matriculaAnteriorId = criarMatricula(alunoId, turmaAnteriorId, periodoAnteriorId);
        atualizarStatusMatricula(matriculaAnteriorId, "EFETIVADA", "Aluno ainda cursando");

        String request = """
                {
                  "turmaId": "%s",
                  "periodoLetivoId": "%s"
                }
                """.formatted(turmaNovaId, periodoNovoId);

        mockMvc.perform(post("/api/matriculas/{id}/rematricula", matriculaAnteriorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Rematrícula não permitida: matrícula base deve estar concluída para renovação"));
    }

    @Test
    @WithMockUser
    void deveConsultarInelegibilidadeDeRematriculaQuandoBaseNaoEstiverConcluida() throws Exception {
        UUID alunoId = criarAluno("Aluno Inelegivel Rematricula", cpfAleatorio(), "aluno.inelegivel.rematricula@example.com");
        UUID segundaSerieId = criarSerie("MATRICULA-2-ANO-INELEG", 2);
        UUID periodoAnteriorId = criarPeriodo("MATRICULA-2044.1", "2044-02-01", "2044-12-15");
        UUID periodoNovoId = criarPeriodo("MATRICULA-2045.1", "2045-02-01", "2045-12-15");
        UUID turmaAnteriorId = criarTurma("MATR-ANT-INE", "Matricula Turma Anterior Inelegivel", 30, periodoAnteriorId);
        UUID turmaNovaId = criarTurma(
                "MATR-REN-INE",
                "Matricula Turma Renovacao Inelegivel",
                30,
                periodoNovoId,
                segundaSerieId);
        UUID matriculaAnteriorId = criarMatricula(alunoId, turmaAnteriorId, periodoAnteriorId);
        atualizarStatusMatricula(matriculaAnteriorId, "EFETIVADA", "Aluno ainda cursando");

        mockMvc.perform(get("/api/matriculas/{id}/rematricula/elegibilidade", matriculaAnteriorId)
                        .param("turmaId", turmaNovaId.toString())
                        .param("periodoLetivoId", periodoNovoId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matriculaBaseId").value(matriculaAnteriorId.toString()))
                .andExpect(jsonPath("$.statusBase").value("EFETIVADA"))
                .andExpect(jsonPath("$.elegivel").value(false))
                .andExpect(jsonPath("$.motivos[0]").value("Matrícula base deve estar concluída para renovação"));
    }

    @Test
    @WithMockUser
    void deveConcluirAcademicamenteMatriculaAprovadaPorBoletimFechado() throws Exception {
        UUID alunoId = criarAluno("Aluno Conclusao Academica", cpfAleatorio(), "aluno.conclusao.academica@example.com");
        UUID periodoId = criarPeriodo("MATRICULA-2037.1", "2037-02-01", "2037-12-15");
        UUID turmaId = criarTurma("MATR-CONC", "Matricula Turma Conclusao", 30, periodoId);
        UUID matriculaId = criarMatricula(alunoId, turmaId, periodoId);
        atualizarStatusMatricula(matriculaId, "EFETIVADA", "Aluno cursando");
        UUID disciplinaId = criarDisciplina("MATRICULA-Conclusao-Matematica", 80);
        UUID boletimId = criarBoletimFechado(matriculaId, disciplinaId, "APROVADO");

        String request = """
                {
                  "boletimId": "%s",
                  "observacao": "Fechamento aprovado pela secretaria"
                }
                """.formatted(boletimId);

        mockMvc.perform(post("/api/matriculas/{id}/conclusao-academica", matriculaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matriculaId").value(matriculaId.toString()))
                .andExpect(jsonPath("$.boletimId").value(boletimId.toString()))
                .andExpect(jsonPath("$.resultadoFinal").value("APROVADO"))
                .andExpect(jsonPath("$.status").value("CONCLUIDA"))
                .andExpect(jsonPath("$.observacao").value(org.hamcrest.Matchers.containsString("Fechamento aprovado pela secretaria")));

        mockMvc.perform(get("/api/matriculas")
                        .param("alunoId", alunoId.toString())
                        .param("status", "CONCLUIDA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(matriculaId.toString()))
                .andExpect(jsonPath("$[0].status").value("CONCLUIDA"));
    }

    @Test
    @WithMockUser
    void deveConfigurarAtualizarListarEExcluirDocumentoExigidoPorTipoMatricula() throws Exception {
        String request = """
                {
                  "tipoMatriculaId": "%s",
                  "tipoDocumentoId": "%s",
                  "obrigatorio": true,
                  "ordem": 1
                }
                """.formatted(TIPO_MATRICULA_PRIMEIRA_ID, TIPO_DOCUMENTO_HISTORICO_ID);

        String responseBody = mockMvc.perform(post("/api/matriculas/catalogos/documentos-exigidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoMatriculaId").value(TIPO_MATRICULA_PRIMEIRA_ID.toString()))
                .andExpect(jsonPath("$.tipoMatricula").value("PRIMEIRA_MATRICULA"))
                .andExpect(jsonPath("$.tipoDocumentoId").value(TIPO_DOCUMENTO_HISTORICO_ID.toString()))
                .andExpect(jsonPath("$.tipoDocumento").value("HISTORICO_ESCOLAR"))
                .andExpect(jsonPath("$.obrigatorio").value(true))
                .andExpect(jsonPath("$.ordem").value(1))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID exigidoId = UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());

        mockMvc.perform(post("/api/matriculas/catalogos/documentos-exigidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/matriculas/catalogos/documentos-exigidos")
                        .param("tipoMatriculaId", TIPO_MATRICULA_PRIMEIRA_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(exigidoId.toString()))
                .andExpect(jsonPath("$[0].tipoDocumento").value("HISTORICO_ESCOLAR"));

        String updateRequest = """
                {
                  "tipoMatriculaId": "%s",
                  "tipoDocumentoId": "%s",
                  "obrigatorio": false,
                  "ordem": 2
                }
                """.formatted(TIPO_MATRICULA_PRIMEIRA_ID, TIPO_DOCUMENTO_CPF_ID);

        mockMvc.perform(put("/api/matriculas/catalogos/documentos-exigidos/{id}", exigidoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(exigidoId.toString()))
                .andExpect(jsonPath("$.tipoDocumento").value("CPF"))
                .andExpect(jsonPath("$.obrigatorio").value(false))
                .andExpect(jsonPath("$.ordem").value(2));

        mockMvc.perform(delete("/api/matriculas/catalogos/documentos-exigidos/{id}", exigidoId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/matriculas/catalogos/documentos-exigidos")
                        .param("tipoMatriculaId", TIPO_MATRICULA_PRIMEIRA_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    private UUID criarAluno(String nome, String cpf, String email) throws Exception {
        String requestBody = """
                {
                  "nomeCompleto": "%s",
                  "cpf": "%s",
                  "email": "%s",
                  "dataNascimento": "2010-05-15"
                }
                """.formatted(nome, cpf, email);

        String responseBody = mockMvc.perform(post("/api/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());
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
        return criarTurma(codigo, nome, capacidade, periodoId, SERIE_PADRAO_ID);
    }

    private UUID criarTurma(String codigo, String nome, int capacidade, UUID periodoId, UUID serieId) throws Exception {
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
                """.formatted(codigo, nome, capacidade, periodoId, serieId);

        String responseBody = mockMvc.perform(post("/api/turmas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());
    }

    private UUID criarSerie(String nome, int ordem) throws Exception {
        String requestBody = """
                {
                  "nome": "%s",
                  "ordem": %d,
                  "nivelEnsino": "Ensino Fundamental"
                }
                """.formatted(nome, ordem);

        String responseBody = mockMvc.perform(post("/api/series")
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

    private UUID criarDocumentoAluno(UUID alunoId, String nomeArquivo) throws Exception {
        String requestBody = """
                {
                  "alunoId": "%s",
                  "tipoDocumento": "HISTORICO_ESCOLAR",
                  "nomeArquivo": "%s",
                  "urlArquivo": "s3://school-documents/alunos/%s/%s",
                  "observacao": "Documento usado no fluxo de matrícula"
                }
                """.formatted(alunoId, nomeArquivo, alunoId, nomeArquivo);

        String responseBody = mockMvc.perform(post("/api/documentos-alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(responseBody);
        return UUID.fromString(json.get("id").asText());
    }

    private UUID criarDisciplina(String nome, int cargaHoraria) {
        UUID disciplinaId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO disciplina (id_disciplina, nome, carga_horaria, ativo, created_at)
                VALUES (?, ?, ?, true, CURRENT_TIMESTAMP)
                """, disciplinaId, nome, cargaHoraria);
        return disciplinaId;
    }

    private UUID criarBoletimFechado(UUID matriculaId, UUID disciplinaId, String resultado) {
        UUID boletimId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO boletim (id_boletim, id_matricula, periodo_referencia, data_fechamento, created_at)
                VALUES (?, ?, '2037.1', CURRENT_DATE, CURRENT_TIMESTAMP)
                """, boletimId, matriculaId);
        jdbcTemplate.update("""
                INSERT INTO boletim_item (
                    id_boletim_item, id_boletim, id_disciplina, media,
                    frequencia_percentual, resultado, carga_horaria
                ) VALUES (?, ?, ?, 8.50, 100.00, ?, 80)
                """, UUID.randomUUID(), boletimId, disciplinaId, resultado);
        return boletimId;
    }

    private void criarDocumentoExigidoPrimeiraMatricula(String tipoDocumentoId, boolean obrigatorio, int ordem) {
        jdbcTemplate.update("""
                INSERT INTO matricula_documento_exigido (
                    id_matricula_documento_exigido,
                    id_tipo_matricula,
                    id_tipo_documento,
                    obrigatorio,
                    ordem,
                    created_at
                ) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """, UUID.randomUUID(), TIPO_MATRICULA_PRIMEIRA_ID, UUID.fromString(tipoDocumentoId), obrigatorio, ordem);
    }

    private String cpfAleatorio() {
        long cpf = System.nanoTime() % 1_000_000_00000L;
        return String.format("%011d", cpf);
    }
}
