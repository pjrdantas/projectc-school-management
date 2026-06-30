package br.com.escola.dashboard.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.aluno.adapter.out.persistence.repository.SolicitacaoExclusaoAlunoJpaRepository;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardMatriculaStatusResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardSecretariaResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardTurmaVagaResponse;
import br.com.escola.dashboard.application.dto.internal.DashboardAcademicoResumo;
import br.com.escola.dashboard.application.port.internal.DashboardAcademicoPort;
import br.com.escola.institucional.application.dto.EscolaContexto;
import br.com.escola.institucional.application.port.EscolaContextoPort;
import br.com.escola.matricula.adapter.out.persistence.repository.MatriculaJpaRepository;
import br.com.escola.matricula.domain.MatriculaStatus;
import br.com.escola.transferencia.adapter.out.persistence.repository.TransferenciaAlunoJpaRepository;

@Service
public class DashboardSecretariaService {

    private final DashboardAcademicoPort dashboardAcademicoPort;
    private final MatriculaJpaRepository matriculaJpaRepository;
    private final TransferenciaAlunoJpaRepository transferenciaAlunoJpaRepository;
    private final SolicitacaoExclusaoAlunoJpaRepository solicitacaoExclusaoAlunoJpaRepository;
    private final EscolaContextoPort escolaContextoPort;

    public DashboardSecretariaService(
            DashboardAcademicoPort dashboardAcademicoPort,
            MatriculaJpaRepository matriculaJpaRepository,
            TransferenciaAlunoJpaRepository transferenciaAlunoJpaRepository,
            SolicitacaoExclusaoAlunoJpaRepository solicitacaoExclusaoAlunoJpaRepository,
            EscolaContextoPort escolaContextoPort) {
        this.dashboardAcademicoPort = dashboardAcademicoPort;
        this.matriculaJpaRepository = matriculaJpaRepository;
        this.transferenciaAlunoJpaRepository = transferenciaAlunoJpaRepository;
        this.solicitacaoExclusaoAlunoJpaRepository = solicitacaoExclusaoAlunoJpaRepository;
        this.escolaContextoPort = escolaContextoPort;
    }

    @Transactional(readOnly = true)
    public DashboardSecretariaResponse consultar() {
        EscolaContexto contexto = escolaContextoPort.obterContextoPadrao();
        UUID escolaId = contexto.escolaId();
        DashboardAcademicoResumo academico = dashboardAcademicoPort.consultarResumo();

        return new DashboardSecretariaResponse(
                contexto.escolaId(),
                contexto.escolaNome(),
                academico.totalMatriculas(),
                countMatriculasPorStatus(escolaId, MatriculaStatus.SOLICITADA),
                countMatriculasPorStatus(escolaId, MatriculaStatus.EM_ANDAMENTO),
                academico.matriculasAguardandoDocumentos(),
                countMatriculasPorStatus(escolaId, MatriculaStatus.AGUARDANDO_HISTORICO_ESCOLAR),
                matriculaJpaRepository.countMatriculasComDocumentosObrigatoriosPendentesByEscolaId(escolaId),
                academico.matriculasAptasRematricula(),
                academico.boletinsFechados(),
                academico.historicosInternosGerados(),
                transferenciaAlunoJpaRepository.countByAluno_Pessoa_Escola_Id(escolaId),
                solicitacaoExclusaoAlunoJpaRepository.countByStatusIgnoreCaseAndAluno_Pessoa_Escola_Id("PENDENTE", escolaId),
                academico.matriculasPorStatus().stream()
                        .map(item -> new DashboardMatriculaStatusResponse(item.status(), item.total()))
                        .toList(),
                academico.turmasComVagas().stream()
                        .map(item -> new DashboardTurmaVagaResponse(
                                item.turmaId(),
                                item.turmaNome(),
                                item.capacidade(),
                                item.vagasOcupadas(),
                                item.vagasDisponiveis()))
                        .toList());
    }

    private long countMatriculasPorStatus(UUID escolaId, MatriculaStatus status) {
        return matriculaJpaRepository.countByTurma_Escola_IdAndStatus_CodigoIgnoreCase(escolaId, status.name());
    }
}
