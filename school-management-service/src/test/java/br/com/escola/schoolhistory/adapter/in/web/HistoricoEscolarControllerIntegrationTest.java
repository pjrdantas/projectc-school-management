package br.com.escola.schoolhistory.adapter.in.web;

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

@SpringBootTest
@AutoConfigureMockMvc
@Sql(
        statements = {
                "DELETE FROM historico_escolar_item",
                "DELETE FROM historico_escolar"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class HistoricoEscolarControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void deveCriarBuscarListarAtualizarEExcluirHistoricoEscolarDocumental() throws Exception {
        String responseBody = mockMvc.perform(post("/api/historicos-escolares")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(historicoRequest("PAULO JOSE ROCHA DANTAS", "4228", "Matemática", "Português")))
                .andExpect(status().isCreated())
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
                .andExpect(jsonPath("$.componentesCurriculares.length()").value(2));

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
                  "componentesCurriculares": []
                }
                """;

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
                """.formatted(nomeAluno, ra, itens);
    }
}
