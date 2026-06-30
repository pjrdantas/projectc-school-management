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
import br.com.escola.dashboard.adapter.in.web.dto.DashboardAcademicoResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardConfiguracaoResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardDiretorResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardFrontendResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardIndicadorHistoricoResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardProfessorResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardSecretariaResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardUsuarioConfiguracaoResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardWidgetResponse;
import br.com.escola.dashboard.application.dto.internal.DashboardAcademicoResumo;
import br.com.escola.dashboard.application.dto.internal.DashboardDiretorResumo;
import br.com.escola.dashboard.application.dto.internal.DashboardMatriculaStatusResumo;
import br.com.escola.dashboard.application.dto.internal.DashboardProfessorResumo;
import br.com.escola.dashboard.application.dto.internal.DashboardProfessorTurmaResumo;
import br.com.escola.dashboard.application.dto.internal.DashboardSecretariaResumo;
import br.com.escola.dashboard.application.dto.internal.DashboardTurmaVagaResumo;
import br.com.escola.dashboard.application.port.internal.DashboardAcademicoPort;
import br.com.escola.dashboard.application.port.internal.DashboardDiretorPort;
import br.com.escola.dashboard.application.port.internal.DashboardProfessorPort;
import br.com.escola.dashboard.application.port.internal.DashboardSecretariaPort;

@ExtendWith(MockitoExtension.class)
class DashboardFrontendServiceTest {

    private static final DashboardMatriculaStatusResumo MATRICULA_STATUS =
            new DashboardMatriculaStatusResumo("ATIVA", 5);
    private static final DashboardTurmaVagaResumo TURMA_VAGA =
            new DashboardTurmaVagaResumo(UUID.randomUUID(), "Turma A", 30, 25, 5);

    @Mock
    private DashboardAcademicoPort dashboardAcademicoPort;

    @Mock
    private DashboardSecretariaPort dashboardSecretariaPort;

    @Mock
    private DashboardDiretorPort dashboardDiretorPort;

    @Mock
    private DashboardProfessorPort dashboardProfessorPort;

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
        DashboardDiretorResumo resumo = diretorResumo();
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

        when(dashboardDiretorPort.consultarResumo()).thenReturn(resumo);
        when(dashboardAlertaService.consultar("DIRETOR", null)).thenReturn(List.of(alerta));
        when(dashboardConfiguracaoAdminService.listarDashboardsPorPublicoCodigo("DIRETOR")).thenReturn(List.of(dashboard));
        when(dashboardConfiguracaoAdminService.listarWidgets(dashboardId)).thenReturn(List.of(widget));
        when(dashboardUsuarioConfiguracaoService.listar(usuarioId, dashboardId)).thenReturn(List.of(configuracaoUsuario));
        when(dashboardIndicadorSnapshotService.consultarHistorico(eq("DIRETOR"), eq(null), any(LocalDate.class), any(LocalDate.class), eq(null)))
                .thenReturn(List.of(historico));

        DashboardFrontendResponse response = service().consultar("diretor", usuarioId, null);

        assertThat(response.publicoCodigo()).isEqualTo("DIRETOR");
        assertThat(response.usuarioId()).isEqualTo(usuarioId);
        assertThat(response.resumo()).isInstanceOf(DashboardDiretorResponse.class);
        DashboardDiretorResponse resumoResponse = (DashboardDiretorResponse) response.resumo();
        assertThat(resumoResponse.escolaId()).isEqualTo(resumo.escolaId());
        assertThat(resumoResponse.totalMatriculas()).isEqualTo(resumo.totalMatriculas());
        assertThat(resumoResponse.turmasLotadas()).isEqualTo(resumo.turmasLotadas());
        assertThat(response.alertas()).containsExactly(alerta);
        assertThat(response.dashboards()).hasSize(1);
        assertThat(response.dashboards().getFirst().widgets()).hasSize(1);
        assertThat(response.configuracoesUsuario()).containsExactly(configuracaoUsuario);
        assertThat(response.historico()).containsExactly(historico);
        verify(dashboardDiretorPort).consultarResumo();
        verify(dashboardProfessorPort, never()).consultarResumo(any());
    }

    @Test
    void deveMontarPacoteAgregadoParaAcademicoViaPortaInterna() {
        UUID usuarioId = UUID.randomUUID();
        DashboardAcademicoResumo resumo = academicoResumo();

        when(dashboardAcademicoPort.consultarResumo()).thenReturn(resumo);
        when(dashboardAlertaService.consultar("ACADEMICO", null)).thenReturn(List.of());
        when(dashboardConfiguracaoAdminService.listarDashboardsPorPublicoCodigo("ACADEMICO")).thenReturn(List.of());
        when(dashboardIndicadorSnapshotService.consultarHistorico(eq("ACADEMICO"), eq(null), any(LocalDate.class), any(LocalDate.class), eq(null)))
                .thenReturn(List.of());

        DashboardFrontendResponse response = service().consultar("academico", usuarioId, null);

        assertThat(response.publicoCodigo()).isEqualTo("ACADEMICO");
        assertThat(response.resumo()).isInstanceOf(DashboardAcademicoResponse.class);
        DashboardAcademicoResponse resumoResponse = (DashboardAcademicoResponse) response.resumo();
        assertThat(resumoResponse.escolaId()).isEqualTo(resumo.escolaId());
        assertThat(resumoResponse.totalMatriculas()).isEqualTo(resumo.totalMatriculas());
        assertThat(resumoResponse.matriculasPorStatus())
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.status()).isEqualTo(MATRICULA_STATUS.status());
                    assertThat(item.total()).isEqualTo(MATRICULA_STATUS.total());
                });
        assertThat(resumoResponse.turmasComVagas())
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.turmaId()).isEqualTo(TURMA_VAGA.turmaId());
                    assertThat(item.vagasDisponiveis()).isEqualTo(TURMA_VAGA.vagasDisponiveis());
                });
        verify(dashboardAcademicoPort).consultarResumo();
        verify(dashboardProfessorPort, never()).consultarResumo(any());
    }

    @Test
    void deveMontarPacoteAgregadoParaSecretariaViaPortaInterna() {
        UUID usuarioId = UUID.randomUUID();
        DashboardSecretariaResumo resumo = secretariaResumo();

        when(dashboardSecretariaPort.consultarResumo()).thenReturn(resumo);
        when(dashboardAlertaService.consultar("SECRETARIA", null)).thenReturn(List.of());
        when(dashboardConfiguracaoAdminService.listarDashboardsPorPublicoCodigo("SECRETARIA")).thenReturn(List.of());
        when(dashboardIndicadorSnapshotService.consultarHistorico(eq("SECRETARIA"), eq(null), any(LocalDate.class), any(LocalDate.class), eq(null)))
                .thenReturn(List.of());

        DashboardFrontendResponse response = service().consultar("secretaria", usuarioId, null);

        assertThat(response.publicoCodigo()).isEqualTo("SECRETARIA");
        assertThat(response.resumo()).isInstanceOf(DashboardSecretariaResponse.class);
        DashboardSecretariaResponse resumoResponse = (DashboardSecretariaResponse) response.resumo();
        assertThat(resumoResponse.escolaId()).isEqualTo(resumo.escolaId());
        assertThat(resumoResponse.matriculasSolicitadas()).isEqualTo(resumo.matriculasSolicitadas());
        assertThat(resumoResponse.matriculasPorStatus())
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.status()).isEqualTo(MATRICULA_STATUS.status());
                    assertThat(item.total()).isEqualTo(MATRICULA_STATUS.total());
                });
        assertThat(resumoResponse.turmasComVagas())
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.turmaId()).isEqualTo(TURMA_VAGA.turmaId());
                    assertThat(item.capacidade()).isEqualTo(TURMA_VAGA.capacidade());
                });
        verify(dashboardSecretariaPort).consultarResumo();
        verify(dashboardProfessorPort, never()).consultarResumo(any());
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
        DashboardProfessorResumo resumo = professorResumo(professorId);

        when(dashboardProfessorPort.consultarResumo(professorId)).thenReturn(resumo);
        when(dashboardAlertaService.consultar("PROFESSOR", professorId)).thenReturn(List.of());
        when(dashboardConfiguracaoAdminService.listarDashboardsPorPublicoCodigo("PROFESSOR")).thenReturn(List.of());
        when(dashboardIndicadorSnapshotService.consultarHistorico(eq("PROFESSOR"), eq(null), any(LocalDate.class), any(LocalDate.class), eq(professorId)))
                .thenReturn(List.of());

        DashboardFrontendResponse response = service().consultar("professor", null, professorId);

        assertThat(response.professorId()).isEqualTo(professorId);
        assertThat(response.resumo()).isInstanceOf(DashboardProfessorResponse.class);
        DashboardProfessorResponse resumoResponse = (DashboardProfessorResponse) response.resumo();
        assertThat(resumoResponse.professorId()).isEqualTo(professorId);
        assertThat(resumoResponse.turmasVinculadas()).isEqualTo(resumo.turmasVinculadas());
        assertThat(resumoResponse.turmas())
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.turmaId()).isEqualTo(resumo.turmas().getFirst().turmaId());
                    assertThat(item.disciplinaNome()).isEqualTo(resumo.turmas().getFirst().disciplinaNome());
                });
        assertThat(response.configuracoesUsuario()).isEmpty();
        verify(dashboardUsuarioConfiguracaoService, never()).listar(any(), any());
    }

    private DashboardFrontendService service() {
        return new DashboardFrontendService(
                dashboardAcademicoPort,
                dashboardSecretariaPort,
                dashboardDiretorPort,
                dashboardProfessorPort,
                dashboardAlertaService,
                dashboardConfiguracaoAdminService,
                dashboardUsuarioConfiguracaoService,
                dashboardIndicadorSnapshotService,
                30);
    }

    private DashboardDiretorResumo diretorResumo() {
        return new DashboardDiretorResumo(
                UUID.randomUUID(), "Escola teste",
                10, 1, 2, 3, 1, 20, 1, 3, 1, 4, 5, 6, 1, 2, 1, 0, 0, 1, List.of(), List.of());
    }

    private DashboardAcademicoResumo academicoResumo() {
        return new DashboardAcademicoResumo(
                UUID.randomUUID(), "Escola teste",
                12, 2, 7, 5, 1, 4, 3, 9, 1, List.of(MATRICULA_STATUS), List.of(TURMA_VAGA));
    }

    private DashboardSecretariaResumo secretariaResumo() {
        return new DashboardSecretariaResumo(
                UUID.randomUUID(), "Escola teste",
                18, 3, 4, 2, 1, 2, 5, 6, 7, 1, 2, List.of(MATRICULA_STATUS), List.of(TURMA_VAGA));
    }

    private DashboardProfessorResumo professorResumo(UUID professorId) {
        return new DashboardProfessorResumo(
                UUID.randomUUID(),
                "Escola teste",
                professorId,
                1,
                1,
                1,
                1,
                0,
                1,
                0,
                1,
                0,
                List.of(new DashboardProfessorTurmaResumo(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "Turma teste",
                        UUID.randomUUID(),
                        "Disciplina teste")));
    }
}
