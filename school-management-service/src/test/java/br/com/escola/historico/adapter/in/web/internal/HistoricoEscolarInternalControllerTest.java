package br.com.escola.historico.adapter.in.web.internal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import br.com.escola.historico.adapter.in.web.dto.HistoricoEscolarRequest;
import br.com.escola.historico.adapter.in.web.dto.HistoricoEscolarResponse;
import br.com.escola.historico.adapter.in.web.dto.HistoricoEscolarTelaResponse;
import br.com.escola.historico.application.service.HistoricoEscolarService;
import com.fasterxml.jackson.databind.ObjectMapper;

class HistoricoEscolarInternalControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void deveCriarHistoricoNoContratoInterno() throws Exception {
        HistoricoEscolarService service = Mockito.mock(HistoricoEscolarService.class);
        UUID historicoId = UUID.randomUUID();
        when(service.criar(Mockito.any(HistoricoEscolarRequest.class))).thenReturn(new HistoricoEscolarResponse(
                historicoId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Escola",
                "Aluno",
                "RG",
                "RA",
                "RM",
                LocalDate.of(2010, 1, 1),
                "Cidade",
                "SP",
                "Brasil",
                "Escola Municipal",
                "Rua A",
                "Cidade",
                "00000-000",
                "1133334444",
                "escola@example.com",
                2025,
                "ENSINO FUNDAMENTAL",
                LocalDate.of(2026, 7, 12),
                "Diretor",
                "RG",
                "Gerente",
                "RG",
                "123",
                LocalDate.of(2026, 7, 12),
                "1",
                "2",
                "Observacao",
                List.of()));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new HistoricoEscolarInternalController(service)).build();

        mockMvc.perform(post("/internal/historicos-escolares")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(historicoId.toString()))
                .andExpect(jsonPath("$.nomeAluno").value("Aluno"));

        verify(service).criar(Mockito.any(HistoricoEscolarRequest.class));
    }

    @Test
    void deveAtualizarHistoricoNoContratoInterno() throws Exception {
        HistoricoEscolarService service = Mockito.mock(HistoricoEscolarService.class);
        UUID historicoId = UUID.randomUUID();
        when(service.atualizar(Mockito.eq(historicoId), Mockito.any(HistoricoEscolarRequest.class))).thenReturn(new HistoricoEscolarResponse(
                historicoId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Escola",
                "Aluno Atualizado",
                "RG",
                "RA",
                "RM",
                LocalDate.of(2010, 1, 1),
                "Cidade",
                "SP",
                "Brasil",
                "Escola Municipal",
                "Rua A",
                "Cidade",
                "00000-000",
                "1133334444",
                "escola@example.com",
                2025,
                "ENSINO FUNDAMENTAL",
                LocalDate.of(2026, 7, 12),
                "Diretor",
                "RG",
                "Gerente",
                "RG",
                "123",
                LocalDate.of(2026, 7, 12),
                "1",
                "2",
                "Observacao",
                List.of()));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new HistoricoEscolarInternalController(service)).build();

        mockMvc.perform(put("/internal/historicos-escolares/{id}", historicoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(historicoId.toString()))
                .andExpect(jsonPath("$.nomeAluno").value("Aluno Atualizado"));

        verify(service).atualizar(Mockito.eq(historicoId), Mockito.any(HistoricoEscolarRequest.class));
    }

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

    private HistoricoEscolarRequest request() {
        return new HistoricoEscolarRequest(
                UUID.randomUUID(),
                "Aluno",
                "RG",
                "RA",
                "RM",
                LocalDate.of(2010, 1, 1),
                "Cidade",
                "SP",
                "Brasil",
                "Escola Municipal",
                "Rua A",
                "Cidade",
                "00000-000",
                "1133334444",
                "escola@example.com",
                2025,
                "ENSINO FUNDAMENTAL",
                LocalDate.of(2026, 7, 12),
                "Diretor",
                "RG",
                "Gerente",
                "RG",
                "123",
                LocalDate.of(2026, 7, 12),
                "1",
                "2",
                "Observacao",
                List.of());
    }
}
