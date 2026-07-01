package br.com.escola.aluno.adapter.in.web;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(
        statements = {
                "DELETE FROM pessoa_documento",
                "DELETE FROM documento",
                "DELETE FROM transferencia_aluno",
                "DELETE FROM escola WHERE id_escola <> '00000000-0000-0000-0000-000000000047'",
                "DELETE FROM historico_escolar_item",
                "DELETE FROM historico_escolar",
                "DELETE FROM disciplina",
                "DELETE FROM matricula_etapa",
                "DELETE FROM matricula",
                "DELETE FROM aluno_responsavel",
                "DELETE FROM responsavel",
                "DELETE FROM turma",
                "DELETE FROM serie WHERE id_serie <> '00000000-0000-0000-0000-000000000100'",
                "DELETE FROM periodo_letivo",
                "DELETE FROM aluno"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AlunoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @WithMockUser
    void deveBuscarAlunoPorIdQuandoExistir() throws Exception {
        String requestBody = """
                {
                  "nomeCompleto": "João da Silva",
                  "cpf": "12345678901",
                  "email": "joao.silva@example.com",
                  "dataNascimento": "2010-05-15"
                }
                """;

        String responseBody = mockMvc.perform(post("/api/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(responseBody).get("id").asText();

        mockMvc.perform(get("/api/alunos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nomeCompleto").value("João da Silva"))
                .andExpect(jsonPath("$.cpf").value("12345678901"));
    }

    @Test
    @WithMockUser
    void deveBuscarFichaConsolidadaDoAlunoComResponsaveis() throws Exception {
        UUID alunoId = criarAluno("Aluno Ficha", cpfAleatorio(), "aluno.ficha@example.com");
        UUID responsavelId = criarResponsavel("Responsavel Ficha", cpfAleatorio());

        String bodyVinculo = """
                {
                  "idResponsavel": "%s",
                  "parentesco": "MAE",
                  "responsavelFinanceiro": true,
                  "responsavelPedagogico": true,
                  "autorizadoRetirar": true
                }
                """.formatted(responsavelId);

        mockMvc.perform(post("/api/alunos/{idAluno}/responsaveis", alunoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyVinculo))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/alunos/{id}/ficha", alunoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aluno.id").value(alunoId.toString()))
                .andExpect(jsonPath("$.aluno.nomeCompleto").value("Aluno Ficha"))
                .andExpect(jsonPath("$.responsaveis[0].id").value(responsavelId.toString()))
                .andExpect(jsonPath("$.responsaveis[0].nomeCompleto").value("Responsavel Ficha"))
                .andExpect(jsonPath("$.responsaveis[0].parentesco").value("MAE"))
                .andExpect(jsonPath("$.responsaveis[0].responsavelFinanceiro").value(true))
                .andExpect(jsonPath("$.responsaveis[0].responsavelPedagogico").value(true))
                .andExpect(jsonPath("$.responsaveis[0].autorizadoRetirar").value(true));
    }

    @Test
    @WithMockUser
    void deveRetornarNotFoundQuandoAlunoNaoExistir() throws Exception {
        mockMvc.perform(get("/api/alunos/{id}", "00000000-0000-0000-0000-000000000001"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Aluno não encontrado para o id 00000000-0000-0000-0000-000000000001"));
    }

    @Test
    @WithMockUser
    void deveExcluirAlunoComResponsavelVinculado() throws Exception {
        String alunoCpf = cpfAleatorio();
        String responsavelCpf = cpfAleatorio();
        UUID alunoId = criarAluno("Aluno Para Excluir", alunoCpf, "aluno.excluir@example.com");
        UUID responsavelId = criarResponsavel("Responsavel Para Excluir", responsavelCpf);

        mockMvc.perform(post("/api/alunos/{idAluno}/responsaveis/{idResponsavel}", alunoId, responsavelId))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/alunos/{id}", alunoId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/alunos/{id}", alunoId))
                .andExpect(status().isNotFound());

        assertCount("SELECT COUNT(*) FROM aluno_responsavel WHERE id_aluno = ?", alunoId, 0);
        assertCount("SELECT COUNT(*) FROM responsavel WHERE id_responsavel = ?", responsavelId, 0);
        assertCount("SELECT COUNT(*) FROM pessoa WHERE cpf = ?", alunoCpf, 0);
        assertCount("SELECT COUNT(*) FROM pessoa WHERE cpf = ?", responsavelCpf, 0);
    }

    @Test
    @WithMockUser
    void deveExcluirAlunoComHistoricoTransferenciaDocumentoEResponsavelNaoCompartilhado() throws Exception {
        String alunoCpf = cpfAleatorio();
        String responsavelCpf = cpfAleatorio();
        UUID alunoId = criarAluno("Aluno Com Dependencias", alunoCpf, "aluno.dependencias@example.com");
        UUID responsavelId = criarResponsavel("Responsavel Unico", responsavelCpf);

        mockMvc.perform(post("/api/alunos/{idAluno}/responsaveis/{idResponsavel}", alunoId, responsavelId))
                .andExpect(status().isCreated());

        criarDocumento(alunoId);
        criarTransferencia(alunoId);
        criarHistorico("Aluno Com Dependencias");

        mockMvc.perform(delete("/api/alunos/{id}", alunoId))
                .andExpect(status().isNoContent());

        assertCount("""
                SELECT COUNT(*)
                  FROM pessoa_documento pd
                  JOIN aluno a ON a.id_pessoa = pd.id_pessoa
                 WHERE a.id_aluno = ?
                """, alunoId, 0);
        assertCount("SELECT COUNT(*) FROM transferencia_aluno WHERE id_aluno = ?", alunoId, 0);
        assertCount("SELECT COUNT(*) FROM historico_escolar WHERE id_aluno = ?", alunoId, 0);
        assertCount("SELECT COUNT(*) FROM aluno_responsavel WHERE id_aluno = ?", alunoId, 0);
        assertCount("SELECT COUNT(*) FROM responsavel WHERE id_responsavel = ?", responsavelId, 0);
        assertCount("SELECT COUNT(*) FROM pessoa WHERE cpf = ?", alunoCpf, 0);
        assertCount("SELECT COUNT(*) FROM pessoa WHERE cpf = ?", responsavelCpf, 0);
    }

    @Test
    @WithMockUser
    void deveManterResponsavelQuandoEleTambemEstiverVinculadoAOutroAluno() throws Exception {
        UUID alunoExcluidoId = criarAluno("Aluno Compartilhado 1", cpfAleatorio(), "aluno.compartilhado1@example.com");
        UUID alunoMantidoId = criarAluno("Aluno Compartilhado 2", cpfAleatorio(), "aluno.compartilhado2@example.com");
        UUID responsavelId = criarResponsavel("Responsavel Compartilhado", cpfAleatorio());

        mockMvc.perform(post("/api/alunos/{idAluno}/responsaveis/{idResponsavel}", alunoExcluidoId, responsavelId))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/alunos/{idAluno}/responsaveis/{idResponsavel}", alunoMantidoId, responsavelId))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/alunos/{id}", alunoExcluidoId))
                .andExpect(status().isNoContent());

        assertCount("SELECT COUNT(*) FROM aluno_responsavel WHERE id_aluno = ?", alunoExcluidoId, 0);
        assertCount("SELECT COUNT(*) FROM aluno_responsavel WHERE id_aluno = ?", alunoMantidoId, 1);
        assertCount("SELECT COUNT(*) FROM responsavel WHERE id_responsavel = ?", responsavelId, 1);
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

    private UUID criarResponsavel(String nome, String cpf) throws Exception {
        String requestBody = """
                {
                  "nomeCompleto": "%s",
                  "cpf": "%s",
                  "email": "responsavel.excluir@example.com",
                  "telefone": "11999999999"
                }
                """.formatted(nome, cpf);

        String responseBody = mockMvc.perform(post("/api/responsaveis")
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

    private void criarDocumento(UUID alunoId) {
        UUID documentoId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO documento (
                    id_documento, id_tipo_documento, nome_arquivo, url_arquivo, data_upload, ativo
                ) VALUES (?, '00000000-0000-0000-0000-000000000095', 'historico.pdf', '/documentos/historico.pdf', CURRENT_TIMESTAMP, true)
                """, documentoId);
        jdbcTemplate.update("""
                INSERT INTO pessoa_documento (id_pessoa_documento, id_pessoa, id_documento, created_at)
                SELECT ?, a.id_pessoa, ?, CURRENT_TIMESTAMP
                  FROM aluno a
                 WHERE a.id_aluno = ?
                """, UUID.randomUUID(), documentoId, alunoId);
    }

    private void criarTransferencia(UUID alunoId) {
        UUID escolaOrigemId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO escola (id_escola, nome, created_at)
                VALUES (?, 'Escola Origem', CURRENT_TIMESTAMP)
                """, escolaOrigemId);
        jdbcTemplate.update("""
                INSERT INTO transferencia_aluno (
                    id_transferencia_aluno, id_aluno, id_escola_origem, serie_origem, ano_letivo_origem,
                    data_solicitacao, id_tipo_transferencia, id_status_transferencia, created_at
                ) VALUES (?, ?, ?, '1 ano', '2025', CURRENT_DATE, '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000112', CURRENT_TIMESTAMP)
                """, UUID.randomUUID(), alunoId, escolaOrigemId);
    }

    private void criarHistorico(String nomeAluno) {
        UUID historicoId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO historico_escolar (id_historico_escolar, id_aluno, origem, nome_aluno, created_at)
                SELECT ?, a.id_aluno, 'EXTERNO', p.nome_completo, CURRENT_TIMESTAMP
                  FROM aluno a
                  JOIN pessoa p ON p.id_pessoa = a.id_pessoa
                 WHERE LOWER(p.nome_completo) = LOWER(?)
                """, historicoId, nomeAluno);
        jdbcTemplate.update("""
                INSERT INTO historico_escolar_item (
                    id_historico_escolar_item, id_historico_escolar, componente_curricular, ano_letivo, serie_descricao
                ) VALUES (?, ?, 'Matemática', 2025, '1 ano')
                """, UUID.randomUUID(), historicoId);
    }

    private void assertCount(String sql, Object param, int expected) {
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, param);
        org.assertj.core.api.Assertions.assertThat(count).isEqualTo(expected);
    }
}
