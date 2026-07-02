package br.com.escola.historico.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.escola.historico.adapter.out.persistence.repository.HistoricoEscolarPendenciaJpaRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(
        statements = {
                "DELETE FROM historico_escolar_item",
                "DELETE FROM historico_escolar",
                "DELETE FROM transferencia_aluno WHERE id_transferencia_aluno = '95000000-0000-0000-0000-000000000014'",
                "DELETE FROM matricula WHERE id_matricula = '95000000-0000-0000-0000-000000000013'",
                "DELETE FROM turma WHERE id_turma = '95000000-0000-0000-0000-000000000012'",
                "DELETE FROM periodo_letivo WHERE id_periodo_letivo = '95000000-0000-0000-0000-000000000011'",
                "DELETE FROM serie WHERE id_serie = '95000000-0000-0000-0000-000000000010'",
                "DELETE FROM aluno WHERE id_aluno = '95000000-0000-0000-0000-000000000001'",
                "DELETE FROM pessoa WHERE id_pessoa = '95000000-0000-0000-0000-000000000002'",
                "UPDATE escola SET telefone = '1133334444', email = 'secretaria@escola.com' WHERE id_escola = '00000000-0000-0000-0000-000000000047'",
                "INSERT INTO pessoa (id_pessoa, nome_completo, cpf, rg, data_nascimento, sexo, nacionalidade, naturalidade, id_escola, ativo, created_at) VALUES ('95000000-0000-0000-0000-000000000002', 'PAULO JOSE ROCHA DANTAS', '95000000001', '17.612.153', DATE '1967-08-27', 'MASCULINO', 'BRASIL', 'GUARATINGUETA', '00000000-0000-0000-0000-000000000047', true, CURRENT_TIMESTAMP)",
                "INSERT INTO aluno (id_aluno, id_pessoa, id_status_aluno, ra, rm, emancipado, ativo, created_at) VALUES ('95000000-0000-0000-0000-000000000001', '95000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000021', '4228', 'RM-001', false, true, CURRENT_TIMESTAMP)"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class HistoricoEscolarControllerIntegrationTest {

    private static final String ALUNO_ID = "95000000-0000-0000-0000-000000000001";
    private static final String PESSOA_ID = "95000000-0000-0000-0000-000000000002";
    private static final String ESCOLA_PADRAO_ID = "00000000-0000-0000-0000-000000000047";
    private static final String SERIE_ID = "95000000-0000-0000-0000-000000000010";
    private static final String PERIODO_LETIVO_ID = "95000000-0000-0000-0000-000000000011";
    private static final String TURMA_ID = "95000000-0000-0000-0000-000000000012";
    private static final String MATRICULA_ID = "95000000-0000-0000-0000-000000000013";
    private static final String TRANSFERENCIA_ID = "95000000-0000-0000-0000-000000000014";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HistoricoEscolarPendenciaJpaRepository historicoEscolarPendenciaJpaRepository;

    @Test
    @WithMockUser
    void deveCriarBuscarListarAtualizarEExcluirHistoricoEscolarDocumental() throws Exception {
        String responseBody = mockMvc.perform(post("/api/historicos-escolares")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(historicoRequest("PAULO JOSE ROCHA DANTAS", "4228", "Matemática", "Português")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.alunoId").value(ALUNO_ID))
                .andExpect(jsonPath("$.escolaId").value(ESCOLA_PADRAO_ID))
                .andExpect(jsonPath("$.nomeAluno").value("PAULO JOSE ROCHA DANTAS"))
                .andExpect(jsonPath("$.ra").value("4228"))
                .andExpect(jsonPath("$.anoConclusao").value(1983))
                .andExpect(jsonPath("$.ensinoConcluido").value("ENSINO FUNDAMENTAL"))
                .andExpect(jsonPath("$.componentesCurriculares.length()").value(2))
                .andExpect(jsonPath("$.componentesCurriculares[0].componenteCurricular").value("Matemática"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID historicoId = UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());

        mockMvc.perform(get("/api/historicos-escolares/{id}", historicoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(historicoId.toString()))
                .andExpect(jsonPath("$.alunoId").value(ALUNO_ID))
                .andExpect(jsonPath("$.nomeEscola").value("Escola Municipal Central"))
                .andExpect(jsonPath("$.municipioEscola").value("São Paulo"))
                .andExpect(jsonPath("$.emailEscola").value("secretaria@escola.com"))
                .andExpect(jsonPath("$.componentesCurriculares.length()").value(2));

        mockMvc.perform(get("/api/historicos-escolares/alunos/{alunoId}", ALUNO_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(historicoId.toString()))
                .andExpect(jsonPath("$[0].alunoId").value(ALUNO_ID));

        mockMvc.perform(get("/api/historicos-escolares")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(historicoId.toString()));

        mockMvc.perform(put("/api/historicos-escolares/{id}", historicoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(historicoRequest("PAULO JOSE ROCHA DANTAS", "4228", "História")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(historicoId.toString()))
                .andExpect(jsonPath("$.componentesCurriculares.length()").value(1))
                .andExpect(jsonPath("$.componentesCurriculares[0].componenteCurricular").value("História"));

        mockMvc.perform(delete("/api/historicos-escolares/{id}", historicoId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/historicos-escolares/{id}", historicoId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void deveRejeitarHistoricoSemComponentesCurriculares() throws Exception {
        String requestBody = """
                {
                  "nomeAluno": "Aluno Sem Componentes",
                  "alunoId": "%s",
                  "componentesCurriculares": []
                }
                """.formatted(ALUNO_ID);

        mockMvc.perform(post("/api/historicos-escolares")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Informe ao menos um componente curricular no histórico escolar"));
    }

    @Test
    @WithMockUser
    void deveRejeitarComponenteCurricularDuplicadoNoMesmoHistorico() throws Exception {
        mockMvc.perform(post("/api/historicos-escolares")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(historicoRequest("Aluno Duplicado", "9999", "Matemática", "Matemática")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Componente curricular duplicado no histórico: Matemática, ano letivo 1979, série 5 série"));
    }

    @Test
    @WithMockUser
    @Sql(
            statements = {
                    "INSERT INTO serie (id_serie, id_nivel_ensino, id_escola, nome, ordem, created_at) VALUES ('95000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000042', '00000000-0000-0000-0000-000000000047', '5 série', 5, CURRENT_TIMESTAMP)",
                    "INSERT INTO periodo_letivo (id_periodo_letivo, nome, ano, data_inicio, data_fim, id_escola, ativo, created_at) VALUES ('95000000-0000-0000-0000-000000000011', '2026', 2026, DATE '2026-02-01', DATE '2026-12-20', '00000000-0000-0000-0000-000000000047', true, CURRENT_TIMESTAMP)",
                    "INSERT INTO turma (id_turma, codigo, nome, capacidade, id_periodo_letivo, id_serie, id_turno, id_escola, ativo, created_at) VALUES ('95000000-0000-0000-0000-000000000012', 'TURMA-5A', '5A', 30, '95000000-0000-0000-0000-000000000011', '95000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000051', '00000000-0000-0000-0000-000000000047', true, CURRENT_TIMESTAMP)",
                    "INSERT INTO matricula (id_matricula, id_aluno, id_turma, id_periodo_letivo, id_status_matricula, id_tipo_matricula, data_solicitacao, created_at) VALUES ('95000000-0000-0000-0000-000000000013', '95000000-0000-0000-0000-000000000001', '95000000-0000-0000-0000-000000000012', '95000000-0000-0000-0000-000000000011', '00000000-0000-0000-0000-000000000075', '00000000-0000-0000-0000-000000000062', DATE '2026-01-15', CURRENT_TIMESTAMP)",
                    "INSERT INTO transferencia_aluno (id_transferencia_aluno, id_aluno, id_escola_origem, id_tipo_transferencia, id_status_transferencia, serie_origem, ano_letivo_origem, data_solicitacao, observacao, created_at) VALUES ('95000000-0000-0000-0000-000000000014', '95000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000047', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000113', '4 série', '2025', DATE '2026-01-10', 'Transferência recebida', CURRENT_TIMESTAMP)"
            },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void deveCarregarNovoHistoricoEscolarParaNovaTela() throws Exception {
        mockMvc.perform(get("/api/historicos-escolares/novo")
                        .param("idAluno", ALUNO_ID)
                        .param("idMatricula", MATRICULA_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contexto.idAluno").value(ALUNO_ID))
                .andExpect(jsonPath("$.contexto.idMatricula").value(MATRICULA_ID))
                .andExpect(jsonPath("$.contexto.modo").value("CADASTRO"))
                .andExpect(jsonPath("$.contexto.status").value("RASCUNHO"))
                .andExpect(jsonPath("$.contexto.serieMatriculaAtual").value(5))
                .andExpect(jsonPath("$.contexto.serieConcluidaOrigem").value(4))
                .andExpect(jsonPath("$.cabecalho.governo").value("GOVERNO DO ESTADO DE SÃO PAULO"))
                .andExpect(jsonPath("$.cabecalho.escola").value("Escola padrão"))
                .andExpect(jsonPath("$.cabecalho.telefone").value("1133334444"))
                .andExpect(jsonPath("$.cabecalho.email").value("secretaria@escola.com"))
                .andExpect(jsonPath("$.aluno.nome").value("PAULO JOSE ROCHA DANTAS"))
                .andExpect(jsonPath("$.periodos.length()").value(9))
                .andExpect(jsonPath("$.estudosRealizados.length()").value(1))
                .andExpect(jsonPath("$.pendencias.length()").value(3));
    }

    @Test
    @WithMockUser
    @Sql(
            statements = {
                    "INSERT INTO serie (id_serie, id_nivel_ensino, id_escola, nome, ordem, created_at) VALUES ('95000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000042', '00000000-0000-0000-0000-000000000047', '5 série', 5, CURRENT_TIMESTAMP)",
                    "INSERT INTO periodo_letivo (id_periodo_letivo, nome, ano, data_inicio, data_fim, id_escola, ativo, created_at) VALUES ('95000000-0000-0000-0000-000000000011', '2026', 2026, DATE '2026-02-01', DATE '2026-12-20', '00000000-0000-0000-0000-000000000047', true, CURRENT_TIMESTAMP)",
                    "INSERT INTO turma (id_turma, codigo, nome, capacidade, id_periodo_letivo, id_serie, id_turno, id_escola, ativo, created_at) VALUES ('95000000-0000-0000-0000-000000000012', 'TURMA-5A', '5A', 30, '95000000-0000-0000-0000-000000000011', '95000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000051', '00000000-0000-0000-0000-000000000047', true, CURRENT_TIMESTAMP)",
                    "INSERT INTO matricula (id_matricula, id_aluno, id_turma, id_periodo_letivo, id_status_matricula, id_tipo_matricula, data_solicitacao, created_at) VALUES ('95000000-0000-0000-0000-000000000013', '95000000-0000-0000-0000-000000000001', '95000000-0000-0000-0000-000000000012', '95000000-0000-0000-0000-000000000011', '00000000-0000-0000-0000-000000000075', '00000000-0000-0000-0000-000000000062', DATE '2026-01-15', CURRENT_TIMESTAMP)"
            },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void deveCarregarHistoricoEscolarExistenteParaEdicaoNaNovaTela() throws Exception {
        String responseBody = mockMvc.perform(post("/api/historicos-escolares")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(historicoRequest("PAULO JOSE ROCHA DANTAS", "4228", "Matemática")))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID historicoId = UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());
        assertThat(historicoEscolarPendenciaJpaRepository.countByHistoricoEscolar_IdAndResolvidaFalse(historicoId))
                .isEqualTo(2);

        mockMvc.perform(get("/api/historicos-escolares/{id}/carregamento", historicoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contexto.idHistoricoEscolar").value(historicoId.toString()))
                .andExpect(jsonPath("$.contexto.idAluno").value(ALUNO_ID))
                .andExpect(jsonPath("$.contexto.idMatricula").value(MATRICULA_ID))
                .andExpect(jsonPath("$.contexto.modo").value("EDICAO"))
                .andExpect(jsonPath("$.contexto.status").value("PENDENTE"))
                .andExpect(jsonPath("$.contexto.serieMatriculaAtual").value(5))
                .andExpect(jsonPath("$.cabecalho.escola").value("Escola Municipal Central"))
                .andExpect(jsonPath("$.cabecalho.endereco").value("Rua Central, 100"))
                .andExpect(jsonPath("$.cabecalho.municipio").value("São Paulo"))
                .andExpect(jsonPath("$.cabecalho.cep").value("01001000"))
                .andExpect(jsonPath("$.aluno.nome").value("PAULO JOSE ROCHA DANTAS"))
                .andExpect(jsonPath("$.aluno.nascimentoEstado").value("SP"))
                .andExpect(jsonPath("$.periodos.length()").value(1))
                .andExpect(jsonPath("$.periodos[0].serie").value("5 série"))
                .andExpect(jsonPath("$.baseComum.length()").value(1))
                .andExpect(jsonPath("$.baseComum[0].nome").value("Matemática"))
                .andExpect(jsonPath("$.baseComum[0].valores[0]").value("C"))
                .andExpect(jsonPath("$.pendencias.length()").value(2));
    }

    private String historicoRequest(String nomeAluno, String ra, String... componentes) {
        StringBuilder itens = new StringBuilder();
        for (int i = 0; i < componentes.length; i++) {
            if (i > 0) {
                itens.append(",");
            }
            itens.append("""
                    {
                      "componenteCurricular": "%s",
                      "anoLetivo": 1979,
                      "serie": "5 série",
                      "ciclo": "CICLO II",
                      "notaConceito": "C",
                      "totalAulas": 180,
                      "cargaHoraria": 160
                    }
                    """.formatted(componentes[i]));
        }

        return """
                {
                  "nomeAluno": "%s",
                  "alunoId": "%s",
                  "rgRen": "17.612.153/SP",
                  "ra": "%s",
                  "rm": "RM-001",
                  "dataNascimento": "1967-08-27",
                  "municipioNascimento": "GUARATINGUETA",
                  "estadoNascimento": "SP",
                  "paisNascimento": "BRASIL",
                  "nomeEscola": "Escola Municipal Central",
                  "enderecoEscola": "Rua Central, 100",
                  "municipioEscola": "São Paulo",
                  "cepEscola": "01001000",
                  "telefoneEscola": "1133334444",
                  "emailEscola": "secretaria@escola.com",
                  "anoConclusao": 1983,
                  "ensinoConcluido": "ENSINO FUNDAMENTAL",
                  "dataEmissao": "1983-12-20",
                  "diretorNome": "Diretor Escolar",
                  "diretorRg": "12345678",
                  "gerenteOrganizacaoNome": "Gerente Organização",
                  "gerenteOrganizacaoRg": "87654321",
                  "doeNumero": "123",
                  "doeData": "1984-01-10",
                  "doeVolume": "1",
                  "doePagina": "25",
                  "observacoes": "Histórico emitido conforme registros escolares.",
                  "componentesCurriculares": [%s]
                }
                """.formatted(nomeAluno, ALUNO_ID, ra, itens);
    }
}
