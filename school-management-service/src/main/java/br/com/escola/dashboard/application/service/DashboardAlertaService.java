package br.com.escola.dashboard.application.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.escola.dashboard.adapter.in.web.dto.DashboardAlertaResponse;
import br.com.escola.dashboard.application.dto.internal.DashboardAcademicoResumo;
import br.com.escola.dashboard.application.dto.internal.DashboardDiretorResumo;
import br.com.escola.dashboard.application.dto.internal.DashboardProfessorResumo;
import br.com.escola.dashboard.application.dto.internal.DashboardSecretariaResumo;
import br.com.escola.dashboard.application.port.internal.DashboardAcademicoPort;
import br.com.escola.dashboard.application.port.internal.DashboardDiretorPort;
import br.com.escola.dashboard.application.port.internal.DashboardProfessorPort;
import br.com.escola.dashboard.application.port.internal.DashboardSecretariaPort;

@Service
public class DashboardAlertaService {

    private final DashboardAcademicoPort dashboardAcademicoPort;
    private final DashboardSecretariaPort dashboardSecretariaPort;
    private final DashboardDiretorPort dashboardDiretorPort;
    private final DashboardProfessorPort dashboardProfessorPort;
    private final long limitePadrao;

    public DashboardAlertaService(
            DashboardAcademicoPort dashboardAcademicoPort,
            DashboardSecretariaPort dashboardSecretariaPort,
            DashboardDiretorPort dashboardDiretorPort,
            DashboardProfessorPort dashboardProfessorPort,
            @Value("${dashboard.alertas.limite-padrao:0}") long limitePadrao) {
        this.dashboardAcademicoPort = dashboardAcademicoPort;
        this.dashboardSecretariaPort = dashboardSecretariaPort;
        this.dashboardDiretorPort = dashboardDiretorPort;
        this.dashboardProfessorPort = dashboardProfessorPort;
        this.limitePadrao = limitePadrao;
    }

    @Transactional(readOnly = true)
    public List<DashboardAlertaResponse> consultar(String publicoCodigo, UUID professorId) {
        String codigo = normalizarCodigo(publicoCodigo);
        List<DashboardAlertaResponse> alertas = switch (codigo) {
            case "ACADEMICO" -> alertasAcademico();
            case "SECRETARIA" -> alertasSecretaria();
            case "DIRETOR" -> alertasDiretor();
            case "PROFESSOR" -> alertasProfessor(professorId);
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Alertas de dashboard não suportados para o público " + codigo);
        };

        return alertas.stream()
                .sorted(Comparator
                        .comparingInt((DashboardAlertaResponse alerta) -> pesoSeveridade(alerta.severidade()))
                        .thenComparing(DashboardAlertaResponse::codigo))
                .toList();
    }

    private List<DashboardAlertaResponse> alertasAcademico() {
        DashboardAcademicoResumo dashboard = dashboardAcademicoPort.consultarResumo();
        List<DashboardAlertaResponse> alertas = new ArrayList<>();
        adicionar(alertas, "ACADEMICO", null, "MATRICULAS_AGUARDANDO_DOCUMENTOS", "ATENCAO",
                "Matrículas aguardando documentos",
                "Existem matrículas aguardando entrega de documentos.",
                dashboard.matriculasAguardandoDocumentos(), limitePadrao);
        adicionar(alertas, "ACADEMICO", null, "ALUNOS_REPROVADOS", "CRITICO",
                "Alunos reprovados",
                "Existem alunos com resultado final reprovado.",
                dashboard.alunosReprovados(), limitePadrao);
        return alertas;
    }

    private List<DashboardAlertaResponse> alertasSecretaria() {
        DashboardSecretariaResumo dashboard = dashboardSecretariaPort.consultarResumo();
        List<DashboardAlertaResponse> alertas = new ArrayList<>();
        adicionar(alertas, "SECRETARIA", null, "MATRICULAS_AGUARDANDO_DOCUMENTOS", "ATENCAO",
                "Matrículas aguardando documentos",
                "Existem matrículas aguardando documentos.",
                dashboard.matriculasAguardandoDocumentos(), limitePadrao);
        adicionar(alertas, "SECRETARIA", null, "MATRICULAS_AGUARDANDO_HISTORICO_ESCOLAR", "ATENCAO",
                "Matrículas aguardando histórico escolar",
                "Existem matrículas aguardando histórico escolar.",
                dashboard.matriculasAguardandoHistoricoEscolar(), limitePadrao);
        adicionar(alertas, "SECRETARIA", null, "MATRICULAS_COM_DOCUMENTOS_PENDENTES", "CRITICO",
                "Documentos pendentes",
                "Existem matrículas com documentos pendentes.",
                dashboard.matriculasComDocumentosPendentes(), limitePadrao);
        adicionar(alertas, "SECRETARIA", null, "SOLICITACOES_EXCLUSAO_PENDENTES", "CRITICO",
                "Solicitações de exclusão pendentes",
                "Existem solicitações de exclusão de aluno pendentes.",
                dashboard.solicitacoesExclusaoPendentes(), limitePadrao);
        return alertas;
    }

    private List<DashboardAlertaResponse> alertasDiretor() {
        DashboardDiretorResumo dashboard = dashboardDiretorPort.consultarResumo();
        List<DashboardAlertaResponse> alertas = new ArrayList<>();
        adicionar(alertas, "DIRETOR", null, "MATRICULAS_PENDENTES", "ATENCAO",
                "Matrículas pendentes",
                "Existem matrículas pendentes de conclusão operacional.",
                dashboard.matriculasPendentes(), limitePadrao);
        adicionar(alertas, "DIRETOR", null, "TURMAS_LOTADAS", "CRITICO",
                "Turmas lotadas",
                "Existem turmas sem vagas disponíveis.",
                dashboard.turmasLotadas(), limitePadrao);
        adicionar(alertas, "DIRETOR", null, "AVALIACOES_COM_NOTAS_PENDENTES", "ATENCAO",
                "Avaliações com notas pendentes",
                "Existem avaliações sem lançamento de notas.",
                dashboard.avaliacoesComNotasPendentes(), limitePadrao);
        adicionar(alertas, "DIRETOR", null, "MATRICULAS_COM_DOCUMENTOS_PENDENTES", "CRITICO",
                "Documentos pendentes",
                "Existem matrículas com documentos pendentes.",
                dashboard.matriculasComDocumentosPendentes(), limitePadrao);
        return alertas;
    }

    private List<DashboardAlertaResponse> alertasProfessor(UUID professorId) {
        if (professorId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "professorId é obrigatório para alertas do público PROFESSOR");
        }

        DashboardProfessorResumo dashboard = dashboardProfessorPort.consultarResumo(professorId);
        List<DashboardAlertaResponse> alertas = new ArrayList<>();
        adicionar(alertas, "PROFESSOR", professorId, "FREQUENCIAS_PENDENTES", "CRITICO",
                "Frequências pendentes",
                "Existem aulas realizadas sem registro de frequência do professor.",
                dashboard.frequenciasPendentes(), limitePadrao);
        adicionar(alertas, "PROFESSOR", professorId, "AVALIACOES_COM_NOTAS_PENDENTES", "ATENCAO",
                "Avaliações com notas pendentes",
                "Existem avaliações sem notas lançadas.",
                dashboard.avaliacoesComNotasPendentes(), limitePadrao);
        adicionar(alertas, "PROFESSOR", professorId, "PLANEJAMENTOS_BIMESTRAIS_PENDENTES", "ATENCAO",
                "Planejamentos pendentes",
                "Existem planejamentos bimestrais pendentes de aprovação.",
                dashboard.planejamentosBimestraisPendentes(), limitePadrao);
        return alertas;
    }

    private void adicionar(
            List<DashboardAlertaResponse> alertas,
            String publicoCodigo,
            UUID professorId,
            String codigo,
            String severidade,
            String titulo,
            String mensagem,
            long valor,
            long limite) {
        if (valor <= limite) {
            return;
        }

        alertas.add(new DashboardAlertaResponse(
                publicoCodigo,
                professorId,
                codigo,
                severidade,
                titulo,
                mensagem,
                valor,
                limite));
    }

    private int pesoSeveridade(String severidade) {
        return switch (severidade) {
            case "CRITICO" -> 0;
            case "ATENCAO" -> 1;
            default -> 2;
        };
    }

    private String normalizarCodigo(String codigo) {
        return codigo.trim().toUpperCase(Locale.ROOT);
    }
}
