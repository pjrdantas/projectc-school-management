package br.com.escola.dashboard.adapter.in.web;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
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
                "DELETE FROM transferencia_aluno",
                "DELETE FROM solicitacao_exclusao_aluno",
                "DELETE FROM matricula_documento_entregue",
                "DELETE FROM matricula_documento_exigido",
                "DELETE FROM boletim_item",
                "DELETE FROM boletim",
                "DELETE FROM historico_escolar WHERE observacoes LIKE 'DASHBOARD-SECRETARIA-%'",
                "DELETE FROM matricula_etapa",
                "DELETE FROM matricula",
                "DELETE FROM escola WHERE nome LIKE 'DASHBOARD-SECRETARIA-%'",
                "DELETE FROM turma WHERE codigo LIKE 'DASH-SEC-%'",
                "DELETE FROM periodo_letivo WHERE nome LIKE 'DASHBOARD-SECRETARIA-%'"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
        statements = {
                "DELETE FROM transferencia_aluno",
                "DELETE FROM solicitacao_exclusao_aluno",
                "DELETE FROM matricula_documento_entregue",
                "DELETE FROM matricula_documento_exigido",
                "DELETE FROM boletim_item",
                "DELETE FROM boletim",
                "DELETE FROM historico_escolar WHERE observacoes LIKE 'DASHBOARD-SECRETARIA-%'",
                "DELETE FROM matricula_etapa",
                "DELETE FROM matricula",
                "DELETE FROM escola WHERE nome LIKE 'DASHBOARD-SECRETARIA-%'",
                "DELETE FROM turma WHERE codigo LIKE 'DASH-SEC-%'",
                "DELETE FROM periodo_letivo WHERE nome LIKE 'DASHBOARD-SECRETARIA-%'"
        },
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class DashboardSecretariaControllerIntegrationTest {

    private static final UUID SERIE_PADRAO_ID = UUID.fromString("00000000-0000-0000-0000-000000000100");
    private static final UUID TIPO_MATRICULA_PRIMEIRA_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000061");
    private static final UUID TIPO_DOCUMENTO_HISTORICO_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000095");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @WithMockUser
    void deveConsultarDashboardDaSecretaria() throws Exception {
        UUID alunoId = criarAluno(
                "Aluno Dashboard Secretaria",
                cpfAleatorio(),
                "aluno.dashboard.secretaria.%s@example.com".formatted(System.nanoTime()));
        UUID periodoId = criarPeriodo("DASHBOARD-SECRETARIA-2051.1", "2051-02-01", "2051-12-15");
        UUID turmaId = criarTurma("DASH-SEC-A", "Dashboard Secretaria Turma A", 5, periodoId);
        criarDocumentoExigidoPrimeiraMatricula();
        criarMatricula(alunoId, turmaId, periodoId);
        criarTransferencia(alunoId);
        criarSolicitacaoExclusaoPendente(alunoId);

        mockMvc.perform(get("/api/dashboard/secretaria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.escolaId").value("00000000-0000-0000-0000-000000000047"))
                .andExpect(jsonPath("$.escolaNome").value("Escola padrão"))
                .andExpect(jsonPath("$.totalMatriculas").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.matriculasEmAndamento").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.matriculasComDocumentosPendentes").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.transferencias").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.solicitacoesExclusaoPendentes").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.matriculasPorStatus[?(@.status == 'EM_ANDAMENTO')].total")
                        .value(hasItem(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.turmasComVagas[?(@.turmaNome == 'Dashboard Secretaria Turma A')].vagasDisponiveis")
                        .value(hasItem(4)));
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

    private void criarMatricula(UUID alunoId, UUID turmaId, UUID periodoId) throws Exception {
        String requestBody = """
                {
                  "alunoId": "%s",
                  "turmaId": "%s",
                  "periodoLetivoId": "%s",
                  "tipoMatricula": "PRIMEIRA_MATRICULA"
                }
                """.formatted(alunoId, turmaId, periodoId);

        mockMvc.perform(post("/api/matriculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"));
    }

    private void criarTransferencia(UUID alunoId) throws Exception {
        String requestBody = """
                {
                  "alunoId": "%s",
                  "escolaOrigem": {
                    "nomeEscola": "DASHBOARD-SECRETARIA-Escola Origem"
                  },
                  "serieOrigem": "1 ano",
                  "anoLetivoOrigem": "2050",
                  "dataTransferencia": "2051-01-20",
                  "tipoTransferencia": "ENTRADA",
                  "statusTransferencia": "EM_ANDAMENTO",
                  "motivoTransferencia": "Transferencia para dashboard"
                }
                """.formatted(alunoId);

        mockMvc.perform(post("/api/transferencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());
    }

    private void criarDocumentoExigidoPrimeiraMatricula() {
        jdbcTemplate.update("""
                INSERT INTO matricula_documento_exigido (
                    id_matricula_documento_exigido,
                    id_tipo_matricula,
                    id_tipo_documento,
                    obrigatorio,
                    ordem,
                    created_at
                ) VALUES (?, ?, ?, true, 1, CURRENT_TIMESTAMP)
                """, UUID.randomUUID(), TIPO_MATRICULA_PRIMEIRA_ID, TIPO_DOCUMENTO_HISTORICO_ID);
    }

    private void criarSolicitacaoExclusaoPendente(UUID alunoId) {
        jdbcTemplate.update("""
                INSERT INTO solicitacao_exclusao_aluno (
                    id_solicitacao_exclusao_aluno,
                    id_aluno,
                    solicitado_por,
                    data_solicitacao,
                    motivo,
                    status
                ) VALUES (?, ?, ?, CURRENT_TIMESTAMP, 'Solicitacao para dashboard', 'PENDENTE')
                """, UUID.randomUUID(), alunoId, UUID.randomUUID());
    }

    private String cpfAleatorio() {
        long cpf = System.nanoTime() % 1_000_000_00000L;
        return String.format("%011d", cpf);
    }
}
