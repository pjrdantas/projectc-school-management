package br.com.escola.professor.adapter.in.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.escola.professor.application.dto.internal.AutoridadePedagogicaResumo;
import br.com.escola.professor.application.port.internal.AutoridadePedagogicaPort;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(
        statements = {
                "DELETE FROM sessao_autenticacao WHERE id_usuario = '97000000-0000-0000-0000-000000000501'",
                "DELETE FROM usuario WHERE id_usuario = '97000000-0000-0000-0000-000000000501'",
                "DELETE FROM diario_classe_lancamento WHERE id_diario_classe_lancamento IN ('97000000-0000-0000-0000-000000000301', '97000000-0000-0000-0000-000000000302')",
                "DELETE FROM professor_turma_disciplina WHERE id_professor_turma_disciplina = '97000000-0000-0000-0000-000000000201'",
                "DELETE FROM turma_disciplina WHERE id_turma_disciplina = '97000000-0000-0000-0000-000000000104'",
                "DELETE FROM professor WHERE id_professor = '97000000-0000-0000-0000-000000000106'",
                "DELETE FROM funcionario WHERE id_funcionario IN ('97000000-0000-0000-0000-000000000401', '97000000-0000-0000-0000-000000000402')",
                "DELETE FROM cargo WHERE id_cargo IN ('97000000-0000-0000-0000-000000000411', '97000000-0000-0000-0000-000000000412')",
                "DELETE FROM pessoa WHERE id_pessoa IN ('97000000-0000-0000-0000-000000000105', '97000000-0000-0000-0000-000000000421', '97000000-0000-0000-0000-000000000422')",
                "DELETE FROM disciplina WHERE id_disciplina = '97000000-0000-0000-0000-000000000103'",
                "DELETE FROM turma WHERE id_turma = '97000000-0000-0000-0000-000000000102'",
                "DELETE FROM periodo_letivo WHERE id_periodo_letivo = '97000000-0000-0000-0000-000000000101'"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
        statements = {
                "DELETE FROM sessao_autenticacao WHERE id_usuario = '97000000-0000-0000-0000-000000000501'",
                "DELETE FROM usuario WHERE id_usuario = '97000000-0000-0000-0000-000000000501'",
                "DELETE FROM diario_classe_lancamento WHERE id_diario_classe_lancamento IN ('97000000-0000-0000-0000-000000000301', '97000000-0000-0000-0000-000000000302')",
                "DELETE FROM professor_turma_disciplina WHERE id_professor_turma_disciplina = '97000000-0000-0000-0000-000000000201'",
                "DELETE FROM turma_disciplina WHERE id_turma_disciplina = '97000000-0000-0000-0000-000000000104'",
                "DELETE FROM professor WHERE id_professor = '97000000-0000-0000-0000-000000000106'",
                "DELETE FROM funcionario WHERE id_funcionario IN ('97000000-0000-0000-0000-000000000401', '97000000-0000-0000-0000-000000000402')",
                "DELETE FROM cargo WHERE id_cargo IN ('97000000-0000-0000-0000-000000000411', '97000000-0000-0000-0000-000000000412')",
                "DELETE FROM pessoa WHERE id_pessoa IN ('97000000-0000-0000-0000-000000000105', '97000000-0000-0000-0000-000000000421', '97000000-0000-0000-0000-000000000422')",
                "DELETE FROM disciplina WHERE id_disciplina = '97000000-0000-0000-0000-000000000103'",
                "DELETE FROM turma WHERE id_turma = '97000000-0000-0000-0000-000000000102'",
                "DELETE FROM periodo_letivo WHERE id_periodo_letivo = '97000000-0000-0000-0000-000000000101'"
        },
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class DiarioClasseChecagemControllerIntegrationTest {

    private static final UUID ESCOLA_ID = UUID.fromString("00000000-0000-0000-0000-000000000047");
    private static final UUID LANCAMENTO_ID = UUID.fromString("97000000-0000-0000-0000-000000000301");
    private static final UUID LANCAMENTO_DIRECAO_ID = UUID.fromString("97000000-0000-0000-0000-000000000302");
    private static final UUID COORDENADOR_FUNCIONARIO_ID = UUID.fromString("97000000-0000-0000-0000-000000000401");
    private static final UUID DIRETOR_FUNCIONARIO_ID = UUID.fromString("97000000-0000-0000-0000-000000000402");
    private static final UUID USUARIO_ID = UUID.fromString("97000000-0000-0000-0000-000000000501");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AutoridadePedagogicaPort autoridadePedagogicaPort;

    @Test
    @WithMockUser
    void deveChecarLancamentoPelaCoordenacaoViaEndpointControlado() throws Exception {
        criarCenarioDiario();
        String accessToken = autenticar();
        when(autoridadePedagogicaPort.resolver(accessToken, Set.of("COORDENADOR")))
                .thenReturn(autoridade(COORDENADOR_FUNCIONARIO_ID, "COORDENADOR"));

        mockMvc.perform(post("/api/diarios-classe/lancamentos/{idLancamento}/checagens/coordenacao", LANCAMENTO_ID)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "observacao": "Conferido pela coordenacao"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idLancamento").value(LANCAMENTO_ID.toString()))
                .andExpect(jsonPath("$.escolaId").value(ESCOLA_ID.toString()))
                .andExpect(jsonPath("$.status").value("CHECADO_COORDENACAO"))
                .andExpect(jsonPath("$.checadoCoordenacaoPorFuncionario").value(COORDENADOR_FUNCIONARIO_ID.toString()))
                .andExpect(jsonPath("$.checadoDirecaoPorFuncionario").doesNotExist())
                .andExpect(jsonPath("$.bloqueado").value(true));
    }

    @Test
    @WithMockUser
    void deveChecarLancamentoPelaDirecaoViaEndpointControlado() throws Exception {
        criarCenarioDiario();
        prepararLancamentoChecadoPelaCoordenacao();
        String accessToken = autenticar();
        when(autoridadePedagogicaPort.resolver(accessToken, Set.of("DIRETOR")))
                .thenReturn(autoridade(DIRETOR_FUNCIONARIO_ID, "DIRETOR"));

        mockMvc.perform(post("/api/diarios-classe/lancamentos/{idLancamento}/checagens/direcao", LANCAMENTO_DIRECAO_ID)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "observacao": "Validado pela direcao"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idLancamento").value(LANCAMENTO_DIRECAO_ID.toString()))
                .andExpect(jsonPath("$.status").value("CHECADO_DIRECAO"))
                .andExpect(jsonPath("$.checadoCoordenacaoPorFuncionario").value(COORDENADOR_FUNCIONARIO_ID.toString()))
                .andExpect(jsonPath("$.checadoDirecaoPorFuncionario").value(DIRETOR_FUNCIONARIO_ID.toString()))
                .andExpect(jsonPath("$.bloqueado").value(true));
    }

    @Test
    @WithMockUser
    void deveRejeitarChecagemSemBearerToken() throws Exception {
        mockMvc.perform(post("/api/diarios-classe/lancamentos/{idLancamento}/checagens/coordenacao", LANCAMENTO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    private void criarCenarioDiario() {
        jdbcTemplate.update("""
                INSERT INTO usuario (id_usuario, username, nome, email, senha_hash, ativo, created_at)
                VALUES (?, 'diario-checagem-user', 'Usuario Diario Checagem', 'diario.checagem.user@example.com',
                        ?, true, CURRENT_TIMESTAMP)
                """, USUARIO_ID, passwordEncoder.encode("senha123"));
        jdbcTemplate.update("""
                INSERT INTO periodo_letivo (id_periodo_letivo, nome, ano, data_inicio, data_fim, id_escola, ativo, created_at)
                VALUES ('97000000-0000-0000-0000-000000000101', 'DIARIO-CHECAGEM-2070', 2070, DATE '2070-02-01', DATE '2070-12-20',
                        '00000000-0000-0000-0000-000000000047', true, CURRENT_TIMESTAMP)
                """);
        jdbcTemplate.update("""
                INSERT INTO turma (id_turma, codigo, nome, capacidade, id_periodo_letivo, id_serie, id_escola, ativo, created_at)
                VALUES ('97000000-0000-0000-0000-000000000102', 'DIARIO-CHK', 'Diario Checagem', 30,
                        '97000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000100',
                        '00000000-0000-0000-0000-000000000047', true, CURRENT_TIMESTAMP)
                """);
        jdbcTemplate.update("""
                INSERT INTO disciplina (id_disciplina, nome, carga_horaria, ativo, id_escola, created_at)
                VALUES ('97000000-0000-0000-0000-000000000103', 'Diario Checagem Matematica', 80, true,
                        '00000000-0000-0000-0000-000000000047', CURRENT_TIMESTAMP)
                """);
        jdbcTemplate.update("""
                INSERT INTO turma_disciplina (id_turma_disciplina, id_turma, id_disciplina, carga_horaria, created_at)
                VALUES ('97000000-0000-0000-0000-000000000104', '97000000-0000-0000-0000-000000000102',
                        '97000000-0000-0000-0000-000000000103', 80, CURRENT_TIMESTAMP)
                """);
        jdbcTemplate.update("""
                INSERT INTO pessoa (id_pessoa, nome_completo, cpf, email, id_escola, ativo, created_at)
                VALUES ('97000000-0000-0000-0000-000000000105', 'Professor Diario Checagem', '97000000001',
                        'diario.checagem.professor@example.com', '00000000-0000-0000-0000-000000000047', true, CURRENT_TIMESTAMP)
                """);
        jdbcTemplate.update("""
                INSERT INTO professor (id_professor, id_pessoa, registro_profissional, formacao, ativo, created_at)
                VALUES ('97000000-0000-0000-0000-000000000106', '97000000-0000-0000-0000-000000000105',
                        'RP-DIARIO-CHECAGEM', 'Licenciatura', true, CURRENT_TIMESTAMP)
                """);
        jdbcTemplate.update("""
                INSERT INTO professor_turma_disciplina (id_professor_turma_disciplina, id_professor, id_turma_disciplina, data_inicio, ativo, created_at)
                VALUES ('97000000-0000-0000-0000-000000000201', '97000000-0000-0000-0000-000000000106',
                        '97000000-0000-0000-0000-000000000104', DATE '2070-02-01', true, CURRENT_TIMESTAMP)
                """);
        criarAutoridadesPedagogicas();
        criarLancamento(LANCAMENTO_ID);
        criarLancamento(LANCAMENTO_DIRECAO_ID);
    }

    private void criarAutoridadesPedagogicas() {
        jdbcTemplate.update("""
                INSERT INTO cargo (id_cargo, codigo, descricao)
                VALUES ('97000000-0000-0000-0000-000000000411', 'COORDENADOR', 'Coordenador pedagogico')
                """);
        jdbcTemplate.update("""
                INSERT INTO cargo (id_cargo, codigo, descricao)
                VALUES ('97000000-0000-0000-0000-000000000412', 'DIRETOR', 'Diretor escolar')
                """);
        jdbcTemplate.update("""
                INSERT INTO pessoa (id_pessoa, nome_completo, cpf, email, id_escola, ativo, created_at)
                VALUES ('97000000-0000-0000-0000-000000000421', 'Coordenador Diario', '97000000002',
                        'diario.checagem.coordenador@example.com', '00000000-0000-0000-0000-000000000047', true, CURRENT_TIMESTAMP)
                """);
        jdbcTemplate.update("""
                INSERT INTO pessoa (id_pessoa, nome_completo, cpf, email, id_escola, ativo, created_at)
                VALUES ('97000000-0000-0000-0000-000000000422', 'Diretor Diario', '97000000003',
                        'diario.checagem.diretor@example.com', '00000000-0000-0000-0000-000000000047', true, CURRENT_TIMESTAMP)
                """);
        jdbcTemplate.update("""
                INSERT INTO funcionario (id_funcionario, id_pessoa, id_cargo, ativo, created_at)
                VALUES ('97000000-0000-0000-0000-000000000401', '97000000-0000-0000-0000-000000000421',
                        '97000000-0000-0000-0000-000000000411', true, CURRENT_TIMESTAMP)
                """);
        jdbcTemplate.update("""
                INSERT INTO funcionario (id_funcionario, id_pessoa, id_cargo, ativo, created_at)
                VALUES ('97000000-0000-0000-0000-000000000402', '97000000-0000-0000-0000-000000000422',
                        '97000000-0000-0000-0000-000000000412', true, CURRENT_TIMESTAMP)
                """);
    }

    private void criarLancamento(UUID lancamentoId) {
        jdbcTemplate.update("""
                INSERT INTO diario_classe_lancamento (
                    id_diario_classe_lancamento, id_professor_turma_disciplina, data_lancamento,
                    mes, ano, status, bloqueado, assinatura_professor, data_assinatura, salvo_em, created_at
                )
                VALUES (?, '97000000-0000-0000-0000-000000000201', DATE '2070-03-10',
                        3, 2070, 'BLOQUEADO', true, 'Professor Diario Checagem', DATE '2070-03-10',
                        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, lancamentoId);
    }

    private void prepararLancamentoChecadoPelaCoordenacao() {
        jdbcTemplate.update("""
                UPDATE diario_classe_lancamento
                   SET status = 'CHECADO_COORDENACAO',
                       checado_coordenacao_por_funcionario = ?,
                       checado_coordenacao_em = CURRENT_TIMESTAMP,
                       observacao_coordenacao = 'Conferido pela coordenacao'
                 WHERE id_diario_classe_lancamento = ?
                """, COORDENADOR_FUNCIONARIO_ID, LANCAMENTO_DIRECAO_ID);
    }

    private AutoridadePedagogicaResumo autoridade(UUID funcionarioId, String cargoCodigo) {
        return new AutoridadePedagogicaResumo(
                UUID.randomUUID(),
                ESCOLA_ID,
                funcionarioId,
                UUID.randomUUID(),
                "Autoridade Diario",
                cargoCodigo,
                cargoCodigo,
                List.of("PEDAGOGICO"),
                List.of("DIARIO_CHECAGEM"));
    }

    private String autenticar() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "login": "diario-checagem-user",
                                  "senha": "senha123"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode payload = objectMapper.readTree(login.getResponse().getContentAsString());
        return payload.get("accessToken").asText();
    }
}
