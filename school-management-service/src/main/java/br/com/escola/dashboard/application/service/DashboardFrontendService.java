package br.com.escola.dashboard.application.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.escola.dashboard.adapter.in.web.dto.DashboardConfiguracaoResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardFrontendConfiguracaoResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardFrontendResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardFrontendWidgetResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardUsuarioConfiguracaoResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardWidgetResponse;

@Service
public class DashboardFrontendService {

    private final DashboardAcademicoService dashboardAcademicoService;
    private final DashboardSecretariaService dashboardSecretariaService;
    private final DashboardDiretorService dashboardDiretorService;
    private final DashboardProfessorService dashboardProfessorService;
    private final DashboardAlertaService dashboardAlertaService;
    private final DashboardConfiguracaoAdminService dashboardConfiguracaoAdminService;
    private final DashboardUsuarioConfiguracaoService dashboardUsuarioConfiguracaoService;
    private final DashboardIndicadorSnapshotService dashboardIndicadorSnapshotService;
    private final int historicoDias;

    public DashboardFrontendService(
            DashboardAcademicoService dashboardAcademicoService,
            DashboardSecretariaService dashboardSecretariaService,
            DashboardDiretorService dashboardDiretorService,
            DashboardProfessorService dashboardProfessorService,
            DashboardAlertaService dashboardAlertaService,
            DashboardConfiguracaoAdminService dashboardConfiguracaoAdminService,
            DashboardUsuarioConfiguracaoService dashboardUsuarioConfiguracaoService,
            DashboardIndicadorSnapshotService dashboardIndicadorSnapshotService,
            @Value("${dashboard.frontend.historico-dias:30}") int historicoDias) {
        this.dashboardAcademicoService = dashboardAcademicoService;
        this.dashboardSecretariaService = dashboardSecretariaService;
        this.dashboardDiretorService = dashboardDiretorService;
        this.dashboardProfessorService = dashboardProfessorService;
        this.dashboardAlertaService = dashboardAlertaService;
        this.dashboardConfiguracaoAdminService = dashboardConfiguracaoAdminService;
        this.dashboardUsuarioConfiguracaoService = dashboardUsuarioConfiguracaoService;
        this.dashboardIndicadorSnapshotService = dashboardIndicadorSnapshotService;
        this.historicoDias = historicoDias;
    }

    @Transactional(readOnly = true)
    public DashboardFrontendResponse consultar(String publicoCodigo, UUID usuarioId, UUID professorId) {
        String codigo = normalizarCodigo(publicoCodigo);
        Object resumo = consultarResumo(codigo, professorId);
        List<DashboardFrontendConfiguracaoResponse> dashboards = listarDashboards(codigo);
        List<DashboardUsuarioConfiguracaoResponse> configuracoesUsuario = listarConfiguracoesUsuario(usuarioId, dashboards);
        LocalDate dataFim = LocalDate.now();
        LocalDate dataInicio = dataFim.minusDays(historicoDias);

        return new DashboardFrontendResponse(
                codigo,
                usuarioId,
                professorId,
                resumo,
                dashboardAlertaService.consultar(codigo, professorId),
                dashboards,
                configuracoesUsuario,
                dashboardIndicadorSnapshotService.consultarHistorico(codigo, null, dataInicio, dataFim, professorId));
    }

    private Object consultarResumo(String publicoCodigo, UUID professorId) {
        return switch (publicoCodigo) {
            case "ACADEMICO" -> dashboardAcademicoService.consultar();
            case "SECRETARIA" -> dashboardSecretariaService.consultar();
            case "DIRETOR" -> dashboardDiretorService.consultar();
            case "PROFESSOR" -> {
                if (professorId == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "professorId é obrigatório para dashboard do público PROFESSOR");
                }
                yield dashboardProfessorService.consultar(professorId);
            }
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Dashboard frontend não suportado para o público " + publicoCodigo);
        };
    }

    private List<DashboardFrontendConfiguracaoResponse> listarDashboards(String publicoCodigo) {
        return dashboardConfiguracaoAdminService.listarDashboardsPorPublicoCodigo(publicoCodigo).stream()
                .map(this::toFrontendConfiguracao)
                .toList();
    }

    private DashboardFrontendConfiguracaoResponse toFrontendConfiguracao(DashboardConfiguracaoResponse dashboard) {
        return new DashboardFrontendConfiguracaoResponse(
                dashboard.id(),
                dashboard.codigo(),
                dashboard.nome(),
                dashboard.descricao(),
                dashboard.ativo(),
                dashboardConfiguracaoAdminService.listarWidgets(dashboard.id()).stream()
                        .map(this::toFrontendWidget)
                        .toList());
    }

    private DashboardFrontendWidgetResponse toFrontendWidget(DashboardWidgetResponse widget) {
        return new DashboardFrontendWidgetResponse(
                widget.id(),
                widget.codigo(),
                widget.titulo(),
                widget.descricao(),
                widget.tipoWidget(),
                widget.ordem(),
                widget.queryReferencia(),
                widget.ativo());
    }

    private List<DashboardUsuarioConfiguracaoResponse> listarConfiguracoesUsuario(
            UUID usuarioId,
            List<DashboardFrontendConfiguracaoResponse> dashboards) {
        if (usuarioId == null || dashboards.isEmpty()) {
            return List.of();
        }
        return dashboards.stream()
                .flatMap(dashboard -> dashboardUsuarioConfiguracaoService.listar(usuarioId, dashboard.id()).stream())
                .toList();
    }

    private String normalizarCodigo(String codigo) {
        return codigo.trim().toUpperCase(Locale.ROOT);
    }
}
