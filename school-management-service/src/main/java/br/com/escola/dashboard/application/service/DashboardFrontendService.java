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

import br.com.escola.dashboard.adapter.in.web.dto.DashboardAcademicoResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardConfiguracaoResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardDiretorResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardFrontendConfiguracaoResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardFrontendResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardFrontendWidgetResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardMatriculaStatusResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardProfessorResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardUsuarioConfiguracaoResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardSecretariaResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardTurmaVagaResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardWidgetResponse;
import br.com.escola.dashboard.application.dto.internal.DashboardAcademicoResumo;
import br.com.escola.dashboard.application.dto.internal.DashboardDiretorResumo;
import br.com.escola.dashboard.application.dto.internal.DashboardProfessorResumo;
import br.com.escola.dashboard.application.dto.internal.DashboardSecretariaResumo;
import br.com.escola.dashboard.application.port.internal.DashboardAcademicoPort;
import br.com.escola.dashboard.application.port.internal.DashboardDiretorPort;
import br.com.escola.dashboard.application.port.internal.DashboardProfessorPort;
import br.com.escola.dashboard.application.port.internal.DashboardSecretariaPort;

@Service
public class DashboardFrontendService {

    private final DashboardAcademicoPort dashboardAcademicoPort;
    private final DashboardSecretariaPort dashboardSecretariaPort;
    private final DashboardDiretorPort dashboardDiretorPort;
    private final DashboardProfessorPort dashboardProfessorPort;
    private final DashboardAlertaService dashboardAlertaService;
    private final DashboardConfiguracaoAdminService dashboardConfiguracaoAdminService;
    private final DashboardUsuarioConfiguracaoService dashboardUsuarioConfiguracaoService;
    private final DashboardIndicadorSnapshotService dashboardIndicadorSnapshotService;
    private final int historicoDias;

    public DashboardFrontendService(
            DashboardAcademicoPort dashboardAcademicoPort,
            DashboardSecretariaPort dashboardSecretariaPort,
            DashboardDiretorPort dashboardDiretorPort,
            DashboardProfessorPort dashboardProfessorPort,
            DashboardAlertaService dashboardAlertaService,
            DashboardConfiguracaoAdminService dashboardConfiguracaoAdminService,
            DashboardUsuarioConfiguracaoService dashboardUsuarioConfiguracaoService,
            DashboardIndicadorSnapshotService dashboardIndicadorSnapshotService,
            @Value("${dashboard.frontend.historico-dias:30}") int historicoDias) {
        this.dashboardAcademicoPort = dashboardAcademicoPort;
        this.dashboardSecretariaPort = dashboardSecretariaPort;
        this.dashboardDiretorPort = dashboardDiretorPort;
        this.dashboardProfessorPort = dashboardProfessorPort;
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
            case "ACADEMICO" -> toAcademicoResponse(dashboardAcademicoPort.consultarResumo());
            case "SECRETARIA" -> toSecretariaResponse(dashboardSecretariaPort.consultarResumo());
            case "DIRETOR" -> toDiretorResponse(dashboardDiretorPort.consultarResumo());
            case "PROFESSOR" -> {
                if (professorId == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "professorId é obrigatório para dashboard do público PROFESSOR");
                }
                yield toProfessorResponse(dashboardProfessorPort.consultarResumo(professorId));
            }
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Dashboard frontend não suportado para o público " + publicoCodigo);
        };
    }

    private DashboardAcademicoResponse toAcademicoResponse(DashboardAcademicoResumo resumo) {
        return new DashboardAcademicoResponse(
                resumo.escolaId(),
                resumo.escolaNome(),
                resumo.totalMatriculas(),
                resumo.matriculasAguardandoDocumentos(),
                resumo.matriculasConcluidas(),
                resumo.matriculasEfetivadas(),
                resumo.matriculasAptasRematricula(),
                resumo.boletinsFechados(),
                resumo.historicosInternosGerados(),
                resumo.alunosAprovados(),
                resumo.alunosReprovados(),
                resumo.matriculasPorStatus().stream()
                        .map(item -> new DashboardMatriculaStatusResponse(item.status(), item.total()))
                        .toList(),
                resumo.turmasComVagas().stream()
                        .map(item -> new DashboardTurmaVagaResponse(
                                item.turmaId(),
                                item.turmaNome(),
                                item.capacidade(),
                                item.vagasOcupadas(),
                                item.vagasDisponiveis()))
                        .toList());
    }

    private DashboardSecretariaResponse toSecretariaResponse(DashboardSecretariaResumo resumo) {
        return new DashboardSecretariaResponse(
                resumo.escolaId(),
                resumo.escolaNome(),
                resumo.totalMatriculas(),
                resumo.matriculasSolicitadas(),
                resumo.matriculasEmAndamento(),
                resumo.matriculasAguardandoDocumentos(),
                resumo.matriculasAguardandoHistoricoEscolar(),
                resumo.matriculasComDocumentosPendentes(),
                resumo.matriculasAptasRematricula(),
                resumo.boletinsFechados(),
                resumo.historicosInternosGerados(),
                resumo.transferencias(),
                resumo.solicitacoesExclusaoPendentes(),
                resumo.matriculasPorStatus().stream()
                        .map(item -> new DashboardMatriculaStatusResponse(item.status(), item.total()))
                        .toList(),
                resumo.turmasComVagas().stream()
                        .map(item -> new DashboardTurmaVagaResponse(
                                item.turmaId(),
                                item.turmaNome(),
                                item.capacidade(),
                                item.vagasOcupadas(),
                                item.vagasDisponiveis()))
                        .toList());
    }

    private DashboardDiretorResponse toDiretorResponse(DashboardDiretorResumo resumo) {
        return new DashboardDiretorResponse(
                resumo.escolaId(),
                resumo.escolaNome(),
                resumo.totalMatriculas(),
                resumo.matriculasPendentes(),
                resumo.matriculasConcluidas(),
                resumo.matriculasEfetivadas(),
                resumo.matriculasAptasRematricula(),
                resumo.alunosAtivos(),
                resumo.alunosInativos(),
                resumo.turmasAtivas(),
                resumo.turmasLotadas(),
                resumo.professoresAlocados(),
                resumo.aulasRealizadas(),
                resumo.avaliacoesRegistradas(),
                resumo.avaliacoesComNotasPendentes(),
                resumo.boletinsFechados(),
                resumo.historicosInternosGerados(),
                resumo.transferencias(),
                resumo.solicitacoesExclusaoPendentes(),
                resumo.matriculasComDocumentosPendentes(),
                resumo.matriculasPorStatus().stream()
                        .map(item -> new DashboardMatriculaStatusResponse(item.status(), item.total()))
                        .toList(),
                resumo.turmasComVagas().stream()
                        .map(item -> new DashboardTurmaVagaResponse(
                                item.turmaId(),
                                item.turmaNome(),
                                item.capacidade(),
                                item.vagasOcupadas(),
                                item.vagasDisponiveis()))
                        .toList());
    }

    private DashboardProfessorResponse toProfessorResponse(DashboardProfessorResumo resumo) {
        return new DashboardProfessorResponse(
                resumo.escolaId(),
                resumo.escolaNome(),
                resumo.professorId(),
                resumo.turmasVinculadas(),
                resumo.alocacoesAtivas(),
                resumo.aulasPlanejadas(),
                resumo.aulasRealizadas(),
                resumo.frequenciasPendentes(),
                resumo.avaliacoesRegistradas(),
                resumo.avaliacoesComNotasPendentes(),
                resumo.planejamentosBimestrais(),
                resumo.planejamentosBimestraisPendentes(),
                resumo.turmas().stream()
                        .map(item -> new br.com.escola.dashboard.adapter.in.web.dto.DashboardProfessorTurmaResponse(
                                item.professorTurmaDisciplinaId(),
                                item.turmaId(),
                                item.turmaNome(),
                                item.disciplinaId(),
                                item.disciplinaNome()))
                        .toList());
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
