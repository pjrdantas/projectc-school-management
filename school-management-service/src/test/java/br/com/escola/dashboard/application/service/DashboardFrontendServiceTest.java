package br.com.escola.dashboard.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import br.com.escola.dashboard.adapter.in.web.dto.DashboardAlertaResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardConfiguracaoResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardDiretorResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardFrontendResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardIndicadorHistoricoResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardProfessorResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardUsuarioConfiguracaoResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardWidgetResponse;

@ExtendWith(MockitoExtension.class)
class DashboardFrontendServiceTest {

    @Mock
    private DashboardAcademicoService dashboardAcademicoService;

    @Mock
    private DashboardSecretariaService dashboardSecretariaService;

    @Mock
    private DashboardDiretorService dashboardDiretorService;

    @Mock
    private DashboardProfessorService dashboardProfessorService;

    @Mock
    private DashboardAlertaService dashboardAlertaService;

    @Mock
    private DashboardConfiguracaoAdminService dashboardConfiguracaoAdminService;

    @Mock
    private DashboardUsuarioConfiguracaoService dashboardUsuarioConfiguracaoService;

    @Mock
    private DashboardIndicadorSnapshotService dashboardIndicadorSnapshotService;

    @Test
    void deveMontarPacoteAgregadoParaDiretorComConfiguracaoDoUsuario() {
        UUID usuarioId = UUID.randomUUID();
        UUID dashboardId = UUID.randomUUID();
        UUID widgetId = UUID.randomUUID();
        UUID configuracaoId = UUID.randomUUID();
        DashboardDiretorResponse resumo = diretorResponse();
        DashboardAlertaResponse alerta = new DashboardAlertaResponse(
                "DIRETOR", null, "TURMAS_LOTADAS", "CRITICO", "Turmas lotadas", "Mensagem", 2, 0);
        DashboardConfiguracaoResponse dashboard = new DashboardConfiguracaoResponse(
                dashboardId, UUID.randomUUID(), "DIRETOR", "DASH_DIRETOR", "Dashboard diretor", "Resumo", true);
        DashboardWidgetResponse widget = new DashboardWidgetResponse(
                widgetId, dashboardId, "DASH_DIRETOR", "WIDGET_ALERTAS", "Alertas", "Alertas críticos", "LISTA", 1, "alertas", true);
        DashboardUsuarioConfiguracaoResponse configuracaoUsuario = new DashboardUsuarioConfiguracaoResponse(
                configuracaoId, usuarioId, widgetId, "WIDGET_ALERTAS", "Alertas", dashboardId, "DASH_DIRETOR", true, 1, "{\"compacto\":true}");
        DashboardIndicadorHistoricoResponse historico = new DashboardIndicadorHistoricoResponse(
                "DIRETOR", "TURMAS_LOTADAS", "Turmas lotadas", BigDecimal.ONE, BigDecimal.ZERO, null, List.of());

        when(dashboardDiretorService.consultar()).thenReturn(resumo);
        when(dashboardAlertaService.consultar("DIRETOR", null)).thenReturn(List.of(alerta));
        when(dashboardConfiguracaoAdminService.listarDashboardsPorPublicoCodigo("DIRETOR")).thenReturn(List.of(dashboard));
        when(dashboardConfiguracaoAdminService.listarWidgets(dashboardId)).thenReturn(List.of(widget));
        when(dashboardUsuarioConfiguracaoService.listar(usuarioId, dashboardId)).thenReturn(List.of(configuracaoUsuario));
        when(dashboardIndicadorSnapshotService.consultarHistorico(eq("DIRETOR"), eq(null), any(LocalDate.class), any(LocalDate.class), eq(null)))
                .thenReturn(List.of(historico));

        DashboardFrontendResponse response = service().consultar("diretor", usuarioId, null);

        assertThat(response.publicoCodigo()).isEqualTo("DIRETOR");
        assertThat(response.usuarioId()).isEqualTo(usuarioId);
        assertThat(response.resumo()).isSameAs(resumo);
        assertThat(response.alertas()).containsExactly(alerta);
        assertThat(response.dashboards()).hasSize(1);
        assertThat(response.dashboards().getFirst().widgets()).hasSize(1);
        assertThat(response.configuracoesUsuario()).containsExactly(configuracaoUsuario);
        assertThat(response.historico()).containsExactly(historico);
    }

    @Test
    void deveExigirProfessorIdParaPacoteDoProfessor() {
        assertThatThrownBy(() -> service().consultar("professor", null, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("professorId é obrigatório");
    }

    @Test
    void deveMontarPacoteDoProfessorSemConfiguracaoQuandoUsuarioNaoInformado() {
        UUID professorId = UUID.randomUUID();
        DashboardProfessorResponse resumo = new DashboardProfessorResponse(
                UUID.randomUUID(), "Escola teste", professorId, 1, 1, 1, 1, 0, 1, 0, 1, 0, List.of());

        when(dashboardProfessorService.consultar(professorId)).thenReturn(resumo);
        when(dashboardAlertaService.consultar("PROFESSOR", professorId)).thenReturn(List.of());
        when(dashboardConfiguracaoAdminService.listarDashboardsPorPublicoCodigo("PROFESSOR")).thenReturn(List.of());
        when(dashboardIndicadorSnapshotService.consultarHistorico(eq("PROFESSOR"), eq(null), any(LocalDate.class), any(LocalDate.class), eq(professorId)))
                .thenReturn(List.of());

        DashboardFrontendResponse response = service().consultar("professor", null, professorId);

        assertThat(response.professorId()).isEqualTo(professorId);
        assertThat(response.resumo()).isSameAs(resumo);
        assertThat(response.configuracoesUsuario()).isEmpty();
        verify(dashboardUsuarioConfiguracaoService, never()).listar(any(), any());
    }

    private DashboardFrontendService service() {
        return new DashboardFrontendService(
                dashboardAcademicoService,
                dashboardSecretariaService,
                dashboardDiretorService,
                dashboardProfessorService,
                dashboardAlertaService,
                dashboardConfiguracaoAdminService,
                dashboardUsuarioConfiguracaoService,
                dashboardIndicadorSnapshotService,
                30);
    }

    private DashboardDiretorResponse diretorResponse() {
        return new DashboardDiretorResponse(
                UUID.randomUUID(), "Escola teste",
                10, 1, 2, 3, 1, 20, 1, 3, 1, 4, 5, 6, 1, 2, 1, 0, 0, 1, List.of(), List.of());
    }
}
