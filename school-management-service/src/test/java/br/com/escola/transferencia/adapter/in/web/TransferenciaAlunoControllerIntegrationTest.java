package br.com.escola.transferencia.adapter.in.web;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.escola.compartilhado.viacep.ViaCepResponse;
import br.com.escola.compartilhado.viacep.ViaCepService;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(
        statements = {
                "DELETE FROM transferencia_aluno",
                "DELETE FROM escola",
                "DELETE FROM historico_escolar_item",
                "DELETE FROM historico_escolar",
                "DELETE FROM disciplina",
                "DELETE FROM matricula_etapa",
                "DELETE FROM matricula",
                "DELETE FROM aluno_responsavel",
                "DELETE FROM turma",
                "DELETE FROM serie WHERE id_serie <> '00000000-0000-0000-0000-000000000100'",
                "DELETE FROM periodo_letivo",
                "DELETE FROM aluno"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class TransferenciaAlunoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @SuppressWarnings("removal")
	@MockBean
    private ViaCepService viaCepService;

    @Test
    @WithMockUser
    void deveCriarTransferenciaComEscolaOrigemEEnderecoViaCep() throws Exception {
        when(viaCepService.consultar(anyString())).thenReturn(new ViaCepResponse(
                "01001-000",
                "Praça da Sé",
                "",
                "Sé",
                "São Paulo",
                "SP",
                false));
        when(viaCepService.normalizar("01001-000")).thenReturn("01001000");

        UUID alunoId = criarAluno();

        String requestBody = """
                {
                  "alunoId": "%s",
                  "escolaOrigem": {
                    "nomeEscola": "Escola Municipal de Origem",
                    "codigoInep": "12345678",
                    "cnpj": "12.345.678/0001-99",
                    "cep": "01001-000",
                    "numero": "100",
                    "complemento": "Secretaria"
                  },
                  "serieOrigem": "1 ano",
                  "anoLetivoOrigem": "2025",
                  "dataTransferencia": "2026-01-20",
                  "motivoTransferencia": "Mudança de cidade",
                  "situacaoOrigem": "APROVADO",
                  "tipoTransferencia": "ENTRADA",
                  "statusTransferencia": "CONFIRMADA",
                  "usuarioOperacao": "Secretaria Teste",
                  "observacao": "Histórico recebido da escola de origem."
                }
                """.formatted(alunoId);

        String responseBody = mockMvc.perform(post("/api/transferencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.alunoId").value(alunoId.toString()))
                .andExpect(jsonPath("$.escolaOrigem.nomeEscola").value("Escola Municipal de Origem"))
                .andExpect(jsonPath("$.escolaOrigem.cep").value("01001000"))
                .andExpect(jsonPath("$.escolaOrigem.logradouro").value("Praça da Sé"))
                .andExpect(jsonPath("$.escolaOrigem.bairro").value("Sé"))
                .andExpect(jsonPath("$.escolaOrigem.cidade").value("São Paulo"))
                .andExpect(jsonPath("$.escolaOrigem.uf").value("SP"))
                .andExpect(jsonPath("$.serieOrigem").value("1 ano"))
                .andExpect(jsonPath("$.tipoTransferencia").value("ENTRADA"))
                .andExpect(jsonPath("$.statusTransferencia").value("CONFIRMADA"))
                .andExpect(jsonPath("$.usuarioOperacao").value("Secretaria Teste"))
                .andExpect(jsonPath("$.dataHoraOperacao").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID transferenciaId = UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());

        mockMvc.perform(get("/api/transferencias/{id}", transferenciaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transferenciaId.toString()));

        mockMvc.perform(get("/api/transferencias/alunos/{alunoId}", alunoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(transferenciaId.toString()));
    }

    private UUID criarAluno() throws Exception {
        String requestBody = """
                {
                  "nomeCompleto": "Aluno Transferência",
                  "cpf": "%s",
                  "email": "transferencia@example.com",
                  "telefone": "11999999999",
                  "dataNascimento": "2012-05-10"
                }
                """.formatted(cpfAleatorio());

        String responseBody = mockMvc.perform(post("/api/alunos")
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
