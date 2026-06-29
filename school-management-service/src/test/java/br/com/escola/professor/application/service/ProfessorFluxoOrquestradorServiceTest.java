package br.com.escola.professor.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestClientException;

import br.com.escola.institucional.adapter.out.persistence.entity.EscolaEntity;
import br.com.escola.institucional.application.service.EscolaTenantService;
import br.com.escola.professor.adapter.in.web.dto.ProfessorAlocacaoRequest;
import br.com.escola.professor.adapter.in.web.dto.ProfessorAlocacaoResponse;
import br.com.escola.professor.adapter.in.web.dto.ProfessorRequest;
import br.com.escola.professor.adapter.in.web.dto.ProfessorResponse;
import br.com.escola.professor.application.dto.internal.ProfessorAlocacaoResumo;
import br.com.escola.professor.application.dto.internal.ProfessorResumo;
import br.com.escola.professor.application.port.internal.ProfessorAcademicoPort;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class ProfessorFluxoOrquestradorServiceTest {

    private static final UUID ESCOLA_ID = UUID.fromString("00000000-0000-0000-0000-000000000047");

    @Mock
    private ProfessorService professorService;

    @Mock
    private ProfessorAcademicoPort professorInternalApiClient;

    @Mock
    private EscolaTenantService escolaTenantService;

    private SimpleMeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        meterRegistry = new SimpleMeterRegistry();
        EscolaEntity escola = new EscolaEntity();
        escola.setId(ESCOLA_ID);
        escola.setNome("Escola Padrão");
        when(escolaTenantService.obterOuCriarEscolaPadrao())
                .thenReturn(escola);
    }

    @Test
    void deveUsarClienteInternoQuandoFeatureHabilitada() {
        UUID professorId = UUID.randomUUID();
        ProfessorFluxoOrquestradorService service = novoService(true, true, false);
        when(professorInternalApiClient.buscarProfessor(ESCOLA_ID, professorId))
                .thenReturn(Optional.of(professorResumo(professorId)));

        ProfessorResponse response = service.buscarPorId(professorId);

        assertThat(response.id()).isEqualTo(professorId);
        verify(professorInternalApiClient).buscarProfessor(ESCOLA_ID, professorId);
        verify(professorService, never()).buscarPorId(any());
        assertThat(meterRegistry.get("professor.internal.client.requests")
                .tag("operacao", "buscarPorId")
                .tag("destino", "internal")
                .tag("resultado", "success")
                .counter()
                .count()).isEqualTo(1.0d);
    }

    @Test
    void deveUsarClienteInternoParaListagemQuandoFeatureHabilitada() {
        ProfessorFluxoOrquestradorService service = novoService(true, true, false);
        when(professorInternalApiClient.listarProfessores(ESCOLA_ID))
                .thenReturn(List.of(professorResumo(UUID.randomUUID())));

        List<ProfessorResponse> response = service.listar();

        assertThat(response).hasSize(1);
        verify(professorInternalApiClient).listarProfessores(ESCOLA_ID);
        verify(professorService, never()).listar();
        assertThat(meterRegistry.get("professor.internal.client.requests")
                .tag("operacao", "listar")
                .tag("destino", "internal")
                .tag("resultado", "success")
                .counter()
                .count()).isEqualTo(1.0d);
    }

    @Test
    void deveFazerFallbackParaServicoLocalQuandoClienteInternoFalha() {
        UUID professorId = UUID.randomUUID();
        ProfessorFluxoOrquestradorService service = novoService(true, true, false);
        when(professorInternalApiClient.listarAlocacoes(ESCOLA_ID, professorId))
                .thenThrow(new RestClientException("falha interna"));
        when(professorService.listarAlocacoes(professorId))
                .thenReturn(List.of(alocacaoResponse(professorId)));

        List<ProfessorAlocacaoResponse> response = service.listarAlocacoes(professorId);

        assertThat(response).hasSize(1);
        verify(professorService).listarAlocacoes(professorId);
        assertThat(meterRegistry.get("professor.internal.client.requests")
                .tag("operacao", "listarAlocacoes")
                .tag("destino", "internal")
                .tag("resultado", "error")
                .counter()
                .count()).isEqualTo(1.0d);
        assertThat(meterRegistry.get("professor.internal.client.requests")
                .tag("operacao", "listarAlocacoes")
                .tag("destino", "local")
                .tag("resultado", "fallback")
                .counter()
                .count()).isEqualTo(1.0d);
        assertThat(meterRegistry.get("professor.internal.client.fallbacks")
                .tag("operacao", "listarAlocacoes")
                .tag("causa", "RestClientException")
                .counter()
                .count()).isEqualTo(1.0d);
    }

    @Test
    void deveFazerFallbackParaServicoLocalQuandoListarPorTurmaFalhaNoClienteInterno() {
        UUID turmaId = UUID.randomUUID();
        ProfessorFluxoOrquestradorService service = novoService(true, true, false);
        when(professorInternalApiClient.listarProfessoresPorTurma(ESCOLA_ID, turmaId))
                .thenThrow(new RestClientException("falha interna"));
        when(professorService.listarPorTurma(turmaId))
                .thenReturn(List.of(alocacaoResponse(UUID.randomUUID())));

        List<ProfessorAlocacaoResponse> response = service.listarPorTurma(turmaId);

        assertThat(response).hasSize(1);
        verify(professorService).listarPorTurma(turmaId);
        assertThat(meterRegistry.get("professor.internal.client.requests")
                .tag("operacao", "listarPorTurma")
                .tag("destino", "internal")
                .tag("resultado", "error")
                .counter()
                .count()).isEqualTo(1.0d);
        assertThat(meterRegistry.get("professor.internal.client.requests")
                .tag("operacao", "listarPorTurma")
                .tag("destino", "local")
                .tag("resultado", "fallback")
                .counter()
                .count()).isEqualTo(1.0d);
        assertThat(meterRegistry.get("professor.internal.client.fallbacks")
                .tag("operacao", "listarPorTurma")
                .tag("causa", "RestClientException")
                .counter()
                .count()).isEqualTo(1.0d);
    }

    @Test
    void deveUsarServicoLocalQuandoFeatureEstiverDesabilitada() {
        UUID funcionarioId = UUID.randomUUID();
        ProfessorRequest request = new ProfessorRequest(funcionarioId, "RP-1", "Licenciatura", true);
        ProfessorFluxoOrquestradorService service = novoService(false, true, false);
        when(professorService.criar(request)).thenReturn(professorResponse());

        ProfessorResponse response = service.criar(request);

        assertThat(response.registroProfissional()).isEqualTo("RP-1");
        verify(professorService).criar(request);
        verify(professorInternalApiClient, never()).criarProfessor(any(), any());
        assertThat(meterRegistry.get("professor.internal.client.requests")
                .tag("operacao", "criar")
                .tag("destino", "local")
                .tag("resultado", "feature_disabled")
                .counter()
                .count()).isEqualTo(1.0d);
    }

    @Test
    void devePropagarErroQuandoFallbackEstiverDesabilitado() {
        UUID professorId = UUID.randomUUID();
        ProfessorFluxoOrquestradorService service = novoService(true, false, false);
        when(professorInternalApiClient.buscarProfessor(ESCOLA_ID, professorId))
                .thenThrow(new RestClientException("falha interna"));

        assertThrows(RestClientException.class, () -> service.buscarPorId(professorId));

        verify(professorService, never()).buscarPorId(any());
    }

    @Test
    void deveFazerFallbackLocalNoBuscarPorIdQuandoCutoverAindaNaoEstaAtivo() {
        UUID professorId = UUID.randomUUID();
        ProfessorFluxoOrquestradorService service = novoService(true, true, false);
        when(professorInternalApiClient.buscarProfessor(ESCOLA_ID, professorId))
                .thenThrow(new RestClientException("falha interna"));
        when(professorService.buscarPorId(professorId)).thenReturn(professorResponse());

        ProfessorResponse response = service.buscarPorId(professorId);

        assertThat(response.nomeCompleto()).isEqualTo("Professor Local");
        verify(professorService).buscarPorId(professorId);
        assertThat(meterRegistry.get("professor.internal.client.requests")
                .tag("operacao", "buscarPorId")
                .tag("destino", "local")
                .tag("resultado", "fallback")
                .counter()
                .count()).isEqualTo(1.0d);
    }

    @Test
    void devePropagarErroNoBuscarPorIdSemFallbackQuandoCutoverEstaAtivo() {
        UUID professorId = UUID.randomUUID();
        ProfessorFluxoOrquestradorService service = novoService(true, true, true);
        when(professorInternalApiClient.buscarProfessor(ESCOLA_ID, professorId))
                .thenThrow(new RestClientException("falha interna"));

        assertThrows(RestClientException.class, () -> service.buscarPorId(professorId));

        verify(professorService, never()).buscarPorId(any());
        assertThat(meterRegistry.get("professor.internal.client.requests")
                .tag("operacao", "buscarPorId")
                .tag("destino", "internal")
                .tag("resultado", "error")
                .counter()
                .count()).isEqualTo(1.0d);
    }

    private ProfessorFluxoOrquestradorService novoService(
            boolean enabled,
            boolean fallbackLocalOnError,
            boolean buscarPorIdCutoverEnabled) {
        return new ProfessorFluxoOrquestradorService(
                professorService,
                professorInternalApiClient,
                escolaTenantService,
                meterRegistry,
                enabled,
                fallbackLocalOnError,
                buscarPorIdCutoverEnabled);
    }

    private ProfessorResumo professorResumo(UUID professorId) {
        return new ProfessorResumo(
                professorId,
                UUID.randomUUID(),
                "Professor Interno",
                ESCOLA_ID,
                "Escola Padrão",
                "RP-1",
                "Licenciatura",
                true,
                LocalDateTime.now(),
                LocalDateTime.now());
    }

    private ProfessorResponse professorResponse() {
        return new ProfessorResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Professor Local",
                ESCOLA_ID,
                "Escola Padrão",
                "RP-1",
                "Licenciatura",
                true,
                LocalDateTime.now(),
                LocalDateTime.now());
    }

    private ProfessorAlocacaoResponse alocacaoResponse(UUID professorId) {
        ProfessorAlocacaoResumo resumo = new ProfessorAlocacaoResumo(
                UUID.randomUUID(),
                professorId,
                "Professor Interno",
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Turma A",
                UUID.randomUUID(),
                "Matemática",
                LocalDate.of(2039, 2, 1),
                null,
                true,
                LocalDateTime.now());
        return new ProfessorAlocacaoResponse(
                resumo.id(),
                resumo.professorId(),
                resumo.professorNome(),
                resumo.turmaDisciplinaId(),
                resumo.turmaId(),
                resumo.turmaNome(),
                resumo.disciplinaId(),
                resumo.disciplinaNome(),
                resumo.dataInicio(),
                resumo.dataFim(),
                resumo.ativo(),
                resumo.createdAt());
    }
}
