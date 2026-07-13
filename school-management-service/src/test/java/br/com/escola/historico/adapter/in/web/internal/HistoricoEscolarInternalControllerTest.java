package br.com.escola.historico.adapter.in.web.internal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import br.com.escola.historico.adapter.in.web.dto.HistoricoEscolarTelaResponse;
import br.com.escola.historico.application.service.HistoricoEscolarService;

class HistoricoEscolarInternalControllerTest {

    @Test
    void deveCarregarHistoricoNovoNoContratoInterno() throws Exception {
        UUID alunoId = UUID.randomUUID();
        UUID matriculaId = UUID.randomUUID();
        HistoricoEscolarService service = Mockito.mock(HistoricoEscolarService.class);
        when(service.carregarNovo(alunoId, matriculaId, "CADASTRO"))
                .thenReturn(tela(null, alunoId, matriculaId, "CADASTRO"));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new HistoricoEscolarInternalController(service)).build();

        mockMvc.perform(get("/internal/historicos-escolares/novo")
                        .param("idAluno", alunoId.toString())
                        .param("idMatricula", matriculaId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contexto.idAluno").value(alunoId.toString()))
                .andExpect(jsonPath("$.contexto.idMatricula").value(matriculaId.toString()))
                .andExpect(jsonPath("$.contexto.modo").value("CADASTRO"));

        verify(service).carregarNovo(alunoId, matriculaId, "CADASTRO");
    }

    @Test
    void deveCarregarHistoricoParaEdicaoNoContratoInterno() throws Exception {
        UUID historicoId = UUID.randomUUID();
        UUID alunoId = UUID.randomUUID();
        UUID matriculaId = UUID.randomUUID();
        HistoricoEscolarService service = Mockito.mock(HistoricoEscolarService.class);
        when(service.carregarParaEdicao(historicoId))
                .thenReturn(tela(historicoId, alunoId, matriculaId, "EDICAO"));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new HistoricoEscolarInternalController(service)).build();

        mockMvc.perform(get("/internal/historicos-escolares/{id}/carregamento", historicoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contexto.idHistoricoEscolar").value(historicoId.toString()))
                .andExpect(jsonPath("$.contexto.modo").value("EDICAO"));

        verify(service).carregarParaEdicao(historicoId);
    }

    private HistoricoEscolarTelaResponse tela(UUID historicoId, UUID alunoId, UUID matriculaId, String modo) {
        return new HistoricoEscolarTelaResponse(
                new HistoricoEscolarTelaResponse.Contexto(
                        historicoId,
                        alunoId,
                        matriculaId,
                        modo,
                        "RASCUNHO",
                        6,
                        5,
                        "Escola Origem",
                        "2026-07-12",
                        false),
                new HistoricoEscolarTelaResponse.Cabecalho(
                        "Governo",
                        "Secretaria",
                        "Diretoria",
                        "Escola",
                        "Ato",
                        "Criacao",
                        "Rua",
                        "100",
                        "Centro",
                        "Cidade",
                        "00000-000",
                        "11999999999",
                        "escola@example.com"),
                new HistoricoEscolarTelaResponse.Aluno(
                        "Aluno",
                        "RG",
                        "RA",
                        "Cidade",
                        "SP",
                        "Brasil",
                        "2010-01-01"),
                List.of(),
                List.of(),
                List.of(),
                new HistoricoEscolarTelaResponse.Totais(List.of(), List.of(), List.of(), List.of()),
                List.of(),
                "Observacoes",
                new HistoricoEscolarTelaResponse.Certificado(5, "Diretor", "Escola", "RG", "2026", "DOE", "2026-07-12",
                        "Gerente", "RGG", "Diretor", "RGD"),
                List.of());
    }
}
