package br.com.escola.dashboard.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.escola.dashboard.adapter.in.web.dto.DashboardAcademicoResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardDiretorResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardIndicadorSnapshotRequest;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardIndicadorSnapshotResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardProfessorResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardSecretariaResponse;
import br.com.escola.dashboard.adapter.out.persistence.entity.PublicoDashboardEntity;
import br.com.escola.dashboard.adapter.out.persistence.repository.PublicoDashboardJpaRepository;

@Service
public class DashboardSnapshotGeradorService {

    private final PublicoDashboardJpaRepository publicoDashboardJpaRepository;
    private final DashboardAcademicoService dashboardAcademicoService;
    private final DashboardSecretariaService dashboardSecretariaService;
    private final DashboardDiretorService dashboardDiretorService;
    private final DashboardProfessorService dashboardProfessorService;
    private final DashboardIndicadorSnapshotService dashboardIndicadorSnapshotService;

    public DashboardSnapshotGeradorService(
            PublicoDashboardJpaRepository publicoDashboardJpaRepository,
            DashboardAcademicoService dashboardAcademicoService,
            DashboardSecretariaService dashboardSecretariaService,
            DashboardDiretorService dashboardDiretorService,
            DashboardProfessorService dashboardProfessorService,
            DashboardIndicadorSnapshotService dashboardIndicadorSnapshotService) {
        this.publicoDashboardJpaRepository = publicoDashboardJpaRepository;
        this.dashboardAcademicoService = dashboardAcademicoService;
        this.dashboardSecretariaService = dashboardSecretariaService;
        this.dashboardDiretorService = dashboardDiretorService;
        this.dashboardProfessorService = dashboardProfessorService;
        this.dashboardIndicadorSnapshotService = dashboardIndicadorSnapshotService;
    }

    @Transactional
    public List<DashboardIndicadorSnapshotResponse> gerar(String publicoCodigo, LocalDate referenciaData) {
        String codigo = normalizarCodigo(publicoCodigo);
        LocalDate dataReferencia = referenciaData == null ? LocalDate.now() : referenciaData;
        PublicoDashboardEntity publico = publicoDashboardJpaRepository.findByCodigo(codigo)
                .orElseThrow(() -> notFound("Público de dashboard não encontrado para o código " + publicoCodigo));

        return switch (codigo) {
            case "ACADEMICO" -> gerarAcademico(publico, dataReferencia);
            case "SECRETARIA" -> gerarSecretaria(publico, dataReferencia);
            case "DIRETOR" -> gerarDiretor(publico, dataReferencia);
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Geração automática de snapshots não suportada para o público " + codigo);
        };
    }

    @Transactional
    public List<DashboardIndicadorSnapshotResponse> gerarProfessor(UUID professorId, LocalDate referenciaData) {
        LocalDate dataReferencia = referenciaData == null ? LocalDate.now() : referenciaData;
        PublicoDashboardEntity publico = publicoDashboardJpaRepository.findByCodigo("PROFESSOR")
                .orElseThrow(() -> notFound("Público de dashboard não encontrado para o código PROFESSOR"));
        DashboardProfessorResponse dashboard = dashboardProfessorService.consultar(professorId);
        String prefixo = "PROFESSOR_" + professorId.toString().replace("-", "").toUpperCase(Locale.ROOT) + "_";
        String valorTexto = professorId.toString();

        List<DashboardIndicadorSnapshotRequest> indicadores = new ArrayList<>();
        indicadores.add(indicador(publico, prefixo + "TURMAS_VINCULADAS", "Turmas vinculadas ao professor", dashboard.turmasVinculadas(), valorTexto, dataReferencia));
        indicadores.add(indicador(publico, prefixo + "ALOCACOES_ATIVAS", "Alocações ativas do professor", dashboard.alocacoesAtivas(), valorTexto, dataReferencia));
        indicadores.add(indicador(publico, prefixo + "AULAS_PLANEJADAS", "Aulas planejadas do professor", dashboard.aulasPlanejadas(), valorTexto, dataReferencia));
        indicadores.add(indicador(publico, prefixo + "AULAS_REALIZADAS", "Aulas realizadas do professor", dashboard.aulasRealizadas(), valorTexto, dataReferencia));
        indicadores.add(indicador(publico, prefixo + "FREQUENCIAS_PENDENTES", "Frequências pendentes do professor", dashboard.frequenciasPendentes(), valorTexto, dataReferencia));
        indicadores.add(indicador(publico, prefixo + "AVALIACOES_REGISTRADAS", "Avaliações registradas do professor", dashboard.avaliacoesRegistradas(), valorTexto, dataReferencia));
        indicadores.add(indicador(publico, prefixo + "AVALIACOES_COM_NOTAS_PENDENTES", "Avaliações com notas pendentes do professor", dashboard.avaliacoesComNotasPendentes(), valorTexto, dataReferencia));
        indicadores.add(indicador(publico, prefixo + "PLANEJAMENTOS_BIMESTRAIS", "Planejamentos bimestrais do professor", dashboard.planejamentosBimestrais(), valorTexto, dataReferencia));
        indicadores.add(indicador(publico, prefixo + "PLANEJAMENTOS_BIMESTRAIS_PENDENTES", "Planejamentos bimestrais pendentes do professor", dashboard.planejamentosBimestraisPendentes(), valorTexto, dataReferencia));
        return salvar(indicadores);
    }

    private List<DashboardIndicadorSnapshotResponse> gerarAcademico(PublicoDashboardEntity publico, LocalDate referenciaData) {
        DashboardAcademicoResponse dashboard = dashboardAcademicoService.consultar();
        List<DashboardIndicadorSnapshotRequest> indicadores = new ArrayList<>();
        indicadores.add(indicador(publico, "TOTAL_MATRICULAS", "Total de matrículas", dashboard.totalMatriculas(), referenciaData));
        indicadores.add(indicador(publico, "MATRICULAS_AGUARDANDO_DOCUMENTOS", "Matrículas aguardando documentos", dashboard.matriculasAguardandoDocumentos(), referenciaData));
        indicadores.add(indicador(publico, "MATRICULAS_CONCLUIDAS", "Matrículas concluídas", dashboard.matriculasConcluidas(), referenciaData));
        indicadores.add(indicador(publico, "MATRICULAS_EFETIVADAS", "Matrículas efetivadas", dashboard.matriculasEfetivadas(), referenciaData));
        indicadores.add(indicador(publico, "MATRICULAS_APTAS_REMATRICULA", "Matrículas aptas para rematrícula", dashboard.matriculasAptasRematricula(), referenciaData));
        indicadores.add(indicador(publico, "BOLETINS_FECHADOS", "Boletins fechados", dashboard.boletinsFechados(), referenciaData));
        indicadores.add(indicador(publico, "HISTORICOS_INTERNOS_GERADOS", "Históricos internos gerados", dashboard.historicosInternosGerados(), referenciaData));
        indicadores.add(indicador(publico, "ALUNOS_APROVADOS", "Alunos aprovados", dashboard.alunosAprovados(), referenciaData));
        indicadores.add(indicador(publico, "ALUNOS_REPROVADOS", "Alunos reprovados", dashboard.alunosReprovados(), referenciaData));
        return salvar(indicadores);
    }

    private List<DashboardIndicadorSnapshotResponse> gerarSecretaria(PublicoDashboardEntity publico, LocalDate referenciaData) {
        DashboardSecretariaResponse dashboard = dashboardSecretariaService.consultar();
        List<DashboardIndicadorSnapshotRequest> indicadores = new ArrayList<>();
        indicadores.add(indicador(publico, "TOTAL_MATRICULAS", "Total de matrículas", dashboard.totalMatriculas(), referenciaData));
        indicadores.add(indicador(publico, "MATRICULAS_SOLICITADAS", "Matrículas solicitadas", dashboard.matriculasSolicitadas(), referenciaData));
        indicadores.add(indicador(publico, "MATRICULAS_EM_ANDAMENTO", "Matrículas em andamento", dashboard.matriculasEmAndamento(), referenciaData));
        indicadores.add(indicador(publico, "MATRICULAS_AGUARDANDO_DOCUMENTOS", "Matrículas aguardando documentos", dashboard.matriculasAguardandoDocumentos(), referenciaData));
        indicadores.add(indicador(publico, "MATRICULAS_AGUARDANDO_HISTORICO_ESCOLAR", "Matrículas aguardando histórico escolar", dashboard.matriculasAguardandoHistoricoEscolar(), referenciaData));
        indicadores.add(indicador(publico, "MATRICULAS_COM_DOCUMENTOS_PENDENTES", "Matrículas com documentos pendentes", dashboard.matriculasComDocumentosPendentes(), referenciaData));
        indicadores.add(indicador(publico, "MATRICULAS_APTAS_REMATRICULA", "Matrículas aptas para rematrícula", dashboard.matriculasAptasRematricula(), referenciaData));
        indicadores.add(indicador(publico, "BOLETINS_FECHADOS", "Boletins fechados", dashboard.boletinsFechados(), referenciaData));
        indicadores.add(indicador(publico, "HISTORICOS_INTERNOS_GERADOS", "Históricos internos gerados", dashboard.historicosInternosGerados(), referenciaData));
        indicadores.add(indicador(publico, "TRANSFERENCIAS", "Transferências", dashboard.transferencias(), referenciaData));
        indicadores.add(indicador(publico, "SOLICITACOES_EXCLUSAO_PENDENTES", "Solicitações de exclusão pendentes", dashboard.solicitacoesExclusaoPendentes(), referenciaData));
        return salvar(indicadores);
    }

    private List<DashboardIndicadorSnapshotResponse> gerarDiretor(PublicoDashboardEntity publico, LocalDate referenciaData) {
        DashboardDiretorResponse dashboard = dashboardDiretorService.consultar();
        List<DashboardIndicadorSnapshotRequest> indicadores = new ArrayList<>();
        indicadores.add(indicador(publico, "TOTAL_MATRICULAS", "Total de matrículas", dashboard.totalMatriculas(), referenciaData));
        indicadores.add(indicador(publico, "MATRICULAS_PENDENTES", "Matrículas pendentes", dashboard.matriculasPendentes(), referenciaData));
        indicadores.add(indicador(publico, "MATRICULAS_CONCLUIDAS", "Matrículas concluídas", dashboard.matriculasConcluidas(), referenciaData));
        indicadores.add(indicador(publico, "MATRICULAS_EFETIVADAS", "Matrículas efetivadas", dashboard.matriculasEfetivadas(), referenciaData));
        indicadores.add(indicador(publico, "MATRICULAS_APTAS_REMATRICULA", "Matrículas aptas para rematrícula", dashboard.matriculasAptasRematricula(), referenciaData));
        indicadores.add(indicador(publico, "ALUNOS_ATIVOS", "Alunos ativos", dashboard.alunosAtivos(), referenciaData));
        indicadores.add(indicador(publico, "ALUNOS_INATIVOS", "Alunos inativos", dashboard.alunosInativos(), referenciaData));
        indicadores.add(indicador(publico, "TURMAS_ATIVAS", "Turmas ativas", dashboard.turmasAtivas(), referenciaData));
        indicadores.add(indicador(publico, "TURMAS_LOTADAS", "Turmas lotadas", dashboard.turmasLotadas(), referenciaData));
        indicadores.add(indicador(publico, "PROFESSORES_ALOCADOS", "Professores alocados", dashboard.professoresAlocados(), referenciaData));
        indicadores.add(indicador(publico, "AULAS_REALIZADAS", "Aulas realizadas", dashboard.aulasRealizadas(), referenciaData));
        indicadores.add(indicador(publico, "AVALIACOES_REGISTRADAS", "Avaliações registradas", dashboard.avaliacoesRegistradas(), referenciaData));
        indicadores.add(indicador(publico, "AVALIACOES_COM_NOTAS_PENDENTES", "Avaliações com notas pendentes", dashboard.avaliacoesComNotasPendentes(), referenciaData));
        indicadores.add(indicador(publico, "BOLETINS_FECHADOS", "Boletins fechados", dashboard.boletinsFechados(), referenciaData));
        indicadores.add(indicador(publico, "HISTORICOS_INTERNOS_GERADOS", "Históricos internos gerados", dashboard.historicosInternosGerados(), referenciaData));
        indicadores.add(indicador(publico, "TRANSFERENCIAS", "Transferências", dashboard.transferencias(), referenciaData));
        indicadores.add(indicador(publico, "SOLICITACOES_EXCLUSAO_PENDENTES", "Solicitações de exclusão pendentes", dashboard.solicitacoesExclusaoPendentes(), referenciaData));
        indicadores.add(indicador(publico, "MATRICULAS_COM_DOCUMENTOS_PENDENTES", "Matrículas com documentos pendentes", dashboard.matriculasComDocumentosPendentes(), referenciaData));
        return salvar(indicadores);
    }

    private DashboardIndicadorSnapshotRequest indicador(
            PublicoDashboardEntity publico,
            String codigoIndicador,
            String descricao,
            long valor,
            LocalDate referenciaData) {
        return indicador(publico, codigoIndicador, descricao, valor, null, referenciaData);
    }

    private DashboardIndicadorSnapshotRequest indicador(
            PublicoDashboardEntity publico,
            String codigoIndicador,
            String descricao,
            long valor,
            String valorTexto,
            LocalDate referenciaData) {
        return new DashboardIndicadorSnapshotRequest(
                publico.getId(),
                codigoIndicador,
                descricao,
                BigDecimal.valueOf(valor),
                valorTexto,
                referenciaData);
    }

    private List<DashboardIndicadorSnapshotResponse> salvar(List<DashboardIndicadorSnapshotRequest> indicadores) {
        return indicadores.stream()
                .map(dashboardIndicadorSnapshotService::salvar)
                .toList();
    }

    private String normalizarCodigo(String codigo) {
        return codigo.trim().toUpperCase(Locale.ROOT);
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }
}
