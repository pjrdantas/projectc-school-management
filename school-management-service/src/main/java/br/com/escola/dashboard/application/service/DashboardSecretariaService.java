package br.com.escola.dashboard.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.aluno.adapter.out.persistence.repository.SolicitacaoExclusaoAlunoJpaRepository;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardAcademicoResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardSecretariaResponse;
import br.com.escola.matricula.adapter.out.persistence.repository.MatriculaJpaRepository;
import br.com.escola.matricula.domain.MatriculaStatus;
import br.com.escola.transferencia.adapter.out.persistence.repository.TransferenciaAlunoJpaRepository;

@Service
public class DashboardSecretariaService {

    private final DashboardAcademicoService dashboardAcademicoService;
    private final MatriculaJpaRepository matriculaJpaRepository;
    private final TransferenciaAlunoJpaRepository transferenciaAlunoJpaRepository;
    private final SolicitacaoExclusaoAlunoJpaRepository solicitacaoExclusaoAlunoJpaRepository;

    public DashboardSecretariaService(
            DashboardAcademicoService dashboardAcademicoService,
            MatriculaJpaRepository matriculaJpaRepository,
            TransferenciaAlunoJpaRepository transferenciaAlunoJpaRepository,
            SolicitacaoExclusaoAlunoJpaRepository solicitacaoExclusaoAlunoJpaRepository) {
        this.dashboardAcademicoService = dashboardAcademicoService;
        this.matriculaJpaRepository = matriculaJpaRepository;
        this.transferenciaAlunoJpaRepository = transferenciaAlunoJpaRepository;
        this.solicitacaoExclusaoAlunoJpaRepository = solicitacaoExclusaoAlunoJpaRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSecretariaResponse consultar() {
        DashboardAcademicoResponse academico = dashboardAcademicoService.consultar();

        return new DashboardSecretariaResponse(
                academico.totalMatriculas(),
                countMatriculasPorStatus(MatriculaStatus.SOLICITADA),
                countMatriculasPorStatus(MatriculaStatus.EM_ANDAMENTO),
                academico.matriculasAguardandoDocumentos(),
                countMatriculasPorStatus(MatriculaStatus.AGUARDANDO_HISTORICO_ESCOLAR),
                matriculaJpaRepository.countMatriculasComDocumentosObrigatoriosPendentes(),
                academico.matriculasAptasRematricula(),
                academico.boletinsFechados(),
                academico.historicosInternosGerados(),
                transferenciaAlunoJpaRepository.count(),
                solicitacaoExclusaoAlunoJpaRepository.countByStatusIgnoreCase("PENDENTE"),
                academico.matriculasPorStatus(),
                academico.turmasComVagas());
    }

    private long countMatriculasPorStatus(MatriculaStatus status) {
        return matriculaJpaRepository.countByStatus_CodigoIgnoreCase(status.name());
    }
}
