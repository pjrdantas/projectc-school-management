package br.com.escola.compartilhado.pessoa.controller;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.escola.compartilhado.pessoa.dto.internal.PessoaAlunoResponsaveisResumo;
import br.com.escola.compartilhado.pessoa.dto.internal.PessoaCatalogoResumo;
import br.com.escola.compartilhado.pessoa.dto.internal.PessoaConsultaCadastralPage;
import br.com.escola.compartilhado.pessoa.dto.internal.PessoaEnderecoResumo;
import br.com.escola.compartilhado.pessoa.dto.internal.PessoaResponsavelResumo;
import br.com.escola.compartilhado.pessoa.dto.internal.PessoaResumo;
import br.com.escola.compartilhado.pessoa.dto.EnderecoDados;
import br.com.escola.compartilhado.pessoa.port.internal.PessoaConsultaPort;
import br.com.escola.compartilhado.pessoa.service.PessoaFoundationService;

@SpringBootTest
@AutoConfigureMockMvc
class PessoaInternalControllerIntegrationTest {

    private static final UUID ESCOLA_ID = UUID.fromString("00000000-0000-0000-0000-000000000047");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PessoaConsultaPort pessoaConsultaPort;

    @MockitoBean
    private PessoaFoundationService pessoaEnderecoPort;

    @Test
    @WithMockUser
    void deveExporCatalogosEPessoaPeloAdaptadorInterno() throws Exception {
        UUID tipoPessoaId = UUID.randomUUID();
        UUID tipoEnderecoId = UUID.randomUUID();
        UUID pessoaId = UUID.randomUUID();

        when(pessoaConsultaPort.listarTiposPessoa())
                .thenReturn(List.of(new PessoaCatalogoResumo(tipoPessoaId, "ALUNO", "Aluno")));
        when(pessoaConsultaPort.listarTiposEndereco())
                .thenReturn(List.of(new PessoaCatalogoResumo(tipoEnderecoId, "RESIDENCIAL", "Residencial")));
        when(pessoaConsultaPort.buscarPessoaPorIdEEscola(pessoaId, ESCOLA_ID))
                .thenReturn(Optional.of(new PessoaResumo(
                        pessoaId,
                        "Pessoa Internal Controller",
                        ESCOLA_ID,
                        "Escola Padrao",
                        true)));

        mockMvc.perform(get("/internal/pessoas/catalogos/tipos-pessoa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(tipoPessoaId.toString()))
                .andExpect(jsonPath("$[0].codigo").value("ALUNO"));

        mockMvc.perform(get("/internal/pessoas/catalogos/tipos-endereco"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(tipoEnderecoId.toString()))
                .andExpect(jsonPath("$[0].codigo").value("RESIDENCIAL"));

        mockMvc.perform(get("/internal/pessoas/{id}", pessoaId)
                        .header("X-Escola-Id", ESCOLA_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pessoaId.toString()))
                .andExpect(jsonPath("$.nomeCompleto").value("Pessoa Internal Controller"))
                .andExpect(jsonPath("$.escolaId").value(ESCOLA_ID.toString()));
    }

    @Test
    @WithMockUser
    void deveExporConsultaCadastralPeloAdaptadorInterno() throws Exception {
        UUID alunoId = UUID.randomUUID();
        UUID responsavelId = UUID.randomUUID();
        when(pessoaConsultaPort.consultarCadastroAlunoResponsavel(
                "Aluno",
                null,
                "Responsavel",
                null,
                0,
                10))
                .thenReturn(new PessoaConsultaCadastralPage(
                        List.of(new PessoaAlunoResponsaveisResumo(
                                alunoId,
                                "Aluno Consulta",
                                "12345678901",
                                "aluno.consulta@example.com",
                                "11999999999",
                                LocalDate.of(2014, 3, 10),
                                LocalDateTime.of(2026, 7, 2, 8, 0, 0),
                                List.of(new PessoaResponsavelResumo(
                                        responsavelId,
                                        "Responsavel Consulta",
                                        "98765432100",
                                        "responsavel.consulta@example.com",
                                        "11888888888",
                                        LocalDateTime.of(2026, 7, 2, 8, 30, 0))))),
                        1L,
                        0,
                        10));

        mockMvc.perform(get("/internal/pessoas/consulta-cadastral")
                        .queryParam("nomeAluno", "Aluno")
                        .queryParam("nomeResponsavel", "Responsavel")
                        .queryParam("page", "0")
                        .queryParam("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].idAluno").value(alunoId.toString()))
                .andExpect(jsonPath("$.content[0].responsaveis[0].id").value(responsavelId.toString()));
    }

    @Test
    @WithMockUser
    void deveExporAtualizacaoInternaDeEnderecoPrincipalComContextoEIdempotencia() throws Exception {
        UUID pessoaId = UUID.randomUUID();
        UUID enderecoId = UUID.randomUUID();
        UUID pessoaEnderecoId = UUID.randomUUID();
        when(pessoaEnderecoPort.atualizarEnderecoPrincipalDaPessoa(pessoaId, ESCOLA_ID, new EnderecoDados(
                "01001000",
                "Praca da Se",
                "100",
                "Apto 10",
                "Se",
                "Sao Paulo",
                "SP",
                "RESIDENCIAL",
                true)))
                .thenReturn(new PessoaEnderecoResumo(
                        pessoaEnderecoId,
                        enderecoId,
                        "01001000",
                        "Praca da Se",
                        "100",
                        "Apto 10",
                        "Se",
                        "Sao Paulo",
                        "SP",
                        "RESIDENCIAL",
                        true));

        mockMvc.perform(put("/internal/pessoas/{id}/endereco-principal", pessoaId)
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Correlation-Id", "corr-address-write")
                        .header("Idempotency-Key", "address-write:" + pessoaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipoEnderecoCodigo": "RESIDENCIAL",
                                  "principal": true,
                                  "cep": "01001000",
                                  "logradouro": "Praca da Se",
                                  "numero": "100",
                                  "complemento": "Apto 10",
                                  "bairro": "Se",
                                  "cidade": "Sao Paulo",
                                  "uf": "SP"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commandId").value("address-write:" + pessoaId))
                .andExpect(jsonPath("$.pessoaId").value(pessoaId.toString()))
                .andExpect(jsonPath("$.enderecoId").value(enderecoId.toString()))
                .andExpect(jsonPath("$.pessoaEnderecoId").value(pessoaEnderecoId.toString()))
                .andExpect(jsonPath("$.status").value("monolith_address_write_completed"))
                .andExpect(jsonPath("$.selectedSource").value("school_management_service"))
                .andExpect(jsonPath("$.persistedLocally").value(false))
                .andExpect(jsonPath("$.fallbackRequired").value(false));
    }

    @Test
    @WithMockUser
    void deveExporCleanupInternoDeEnderecosComContextoEIdempotencia() throws Exception {
        UUID pessoaId = UUID.randomUUID();
        when(pessoaConsultaPort.buscarPessoaPorIdEEscola(pessoaId, ESCOLA_ID))
                .thenReturn(Optional.of(new PessoaResumo(
                        pessoaId,
                        "Pessoa Cleanup",
                        ESCOLA_ID,
                        "Escola Padrao",
                        true)));

        mockMvc.perform(delete("/internal/pessoas/{id}/enderecos", pessoaId)
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Correlation-Id", "corr-address-cleanup")
                        .header("Idempotency-Key", "address-cleanup:" + pessoaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commandId").value("address-cleanup:" + pessoaId))
                .andExpect(jsonPath("$.pessoaId").value(pessoaId.toString()))
                .andExpect(jsonPath("$.status").value("monolith_address_cleanup_completed"))
                .andExpect(jsonPath("$.selectedSource").value("school_management_service"))
                .andExpect(jsonPath("$.persistedLocally").value(false))
                .andExpect(jsonPath("$.fallbackRequired").value(false));
        verify(pessoaEnderecoPort).removerEnderecosDaPessoaRemovendoOrfaos(pessoaId);
    }

    @Test
    @WithMockUser
    void deveRejeitarEscritaInternaSemIdempotencyKey() throws Exception {
        mockMvc.perform(put("/internal/pessoas/{id}/endereco-principal", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Correlation-Id", "corr-address-write")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveExigirAutenticacaoNoAdaptadorInterno() throws Exception {
        mockMvc.perform(get("/internal/pessoas/catalogos/tipos-pessoa"))
                .andExpect(status().isUnauthorized());
    }
}
