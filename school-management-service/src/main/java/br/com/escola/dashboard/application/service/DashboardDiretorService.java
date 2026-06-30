package br.com.escola.dashboard.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.aluno.adapter.out.persistence.repository.AlunoJpaRepository;
import br.com.escola.avaliacao.adapter.out.persistence.entity.AvaliacaoEntity;
import br.com.escola.avaliacao.adapter.out.persistence.repository.AvaliacaoJpaRepository;
import br.com.escola.avaliacao.adapter.out.persistence.repository.NotaAlunoJpaRepository;
import br.com.escola.catalogo.adapter.out.persistence.entity.TurmaEntity;
import br.com.escola.catalogo.adapter.out.persistence.repository.TurmaJpaRepository;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardDiretorResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardMatriculaStatusResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardSecretariaResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardTurmaVagaResponse;
import br.com.escola.dashboard.application.dto.internal.DashboardAcademicoResumo;
import br.com.escola.dashboard.application.port.internal.DashboardAcademicoPort;
import br.com.escola.institucional.application.dto.EscolaContexto;
import br.com.escola.institucional.application.port.EscolaContextoPort;
import br.com.escola.matricula.adapter.out.persistence.repository.MatriculaJpaRepository;
import br.com.escola.matricula.domain.MatriculaStatus;
import br.com.escola.professor.adapter.out.persistence.entity.ProfessorTurmaDisciplinaEntity;
import br.com.escola.professor.adapter.out.persistence.repository.AulaJpaRepository;
import br.com.escola.professor.adapter.out.persistence.repository.ProfessorTurmaDisciplinaJpaRepository;

@Service
public class DashboardDiretorService {

    private static final List<String> STATUS_NAO_OCUPAM_VAGA = List.of(
            MatriculaStatus.CANCELADA.name(),
            MatriculaStatus.INDEFERIDA.name(),
            MatriculaStatus.TRANSFERIDO.name());

    private final DashboardAcademicoPort dashboardAcademicoPort;
    private final DashboardSecretariaService dashboardSecretariaService;
    private final AlunoJpaRepository alunoJpaRepository;
    private final TurmaJpaRepository turmaJpaRepository;
    private final MatriculaJpaRepository matriculaJpaRepository;
    private final ProfessorTurmaDisciplinaJpaRepository professorTurmaDisciplinaJpaRepository;
    private final AulaJpaRepository aulaJpaRepository;
    private final AvaliacaoJpaRepository avaliacaoJpaRepository;
    private final NotaAlunoJpaRepository notaAlunoJpaRepository;
    private final EscolaContextoPort escolaContextoPort;

    public DashboardDiretorService(
            DashboardAcademicoPort dashboardAcademicoPort,
            DashboardSecretariaService dashboardSecretariaService,
            AlunoJpaRepository alunoJpaRepository,
            TurmaJpaRepository turmaJpaRepository,
            MatriculaJpaRepository matriculaJpaRepository,
            ProfessorTurmaDisciplinaJpaRepository professorTurmaDisciplinaJpaRepository,
            AulaJpaRepository aulaJpaRepository,
            AvaliacaoJpaRepository avaliacaoJpaRepository,
            NotaAlunoJpaRepository notaAlunoJpaRepository,
            EscolaContextoPort escolaContextoPort) {
        this.dashboardAcademicoPort = dashboardAcademicoPort;
        this.dashboardSecretariaService = dashboardSecretariaService;
        this.alunoJpaRepository = alunoJpaRepository;
        this.turmaJpaRepository = turmaJpaRepository;
        this.matriculaJpaRepository = matriculaJpaRepository;
        this.professorTurmaDisciplinaJpaRepository = professorTurmaDisciplinaJpaRepository;
        this.aulaJpaRepository = aulaJpaRepository;
        this.avaliacaoJpaRepository = avaliacaoJpaRepository;
        this.notaAlunoJpaRepository = notaAlunoJpaRepository;
        this.escolaContextoPort = escolaContextoPort;
    }

    @Transactional(readOnly = true)
    public DashboardDiretorResponse consultar() {
        EscolaContexto contexto = escolaContextoPort.obterContextoPadrao();
        UUID escolaId = contexto.escolaId();
        DashboardAcademicoResumo academico = dashboardAcademicoPort.consultarResumo();
        DashboardSecretariaResponse secretaria = dashboardSecretariaService.consultar();

        return new DashboardDiretorResponse(
                contexto.escolaId(),
                contexto.escolaNome(),
                academico.totalMatriculas(),
                contarMatriculasPendentes(secretaria),
                academico.matriculasConcluidas(),
                academico.matriculasEfetivadas(),
                academico.matriculasAptasRematricula(),
                alunoJpaRepository.countByPessoa_Escola_IdAndAtivoTrue(escolaId),
                alunoJpaRepository.countByPessoa_Escola_IdAndAtivoFalse(escolaId),
                contarTurmasAtivas(escolaId),
                contarTurmasLotadas(escolaId),
                contarProfessoresAlocados(escolaId),
                contarAulasRealizadas(escolaId),
                avaliacaoJpaRepository.findAllByProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(escolaId).size(),
                contarAvaliacoesComNotasPendentes(escolaId),
                academico.boletinsFechados(),
                academico.historicosInternosGerados(),
                secretaria.transferencias(),
                secretaria.solicitacoesExclusaoPendentes(),
                secretaria.matriculasComDocumentosPendentes(),
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

    private long contarMatriculasPendentes(DashboardSecretariaResponse secretaria) {
        return secretaria.matriculasSolicitadas()
                + secretaria.matriculasEmAndamento()
                + secretaria.matriculasAguardandoDocumentos()
                + secretaria.matriculasAguardandoHistoricoEscolar();
    }

    private long contarTurmasAtivas(UUID escolaId) {
        return turmaJpaRepository.findAllByEscola_Id(escolaId).stream()
                .filter(turma -> !Boolean.FALSE.equals(turma.getAtivo()))
                .count();
    }

    private long contarTurmasLotadas(UUID escolaId) {
        return turmaJpaRepository.findAllByEscola_Id(escolaId).stream()
                .filter(turma -> !Boolean.FALSE.equals(turma.getAtivo()))
                .filter(this::isTurmaLotada)
                .count();
    }

    private boolean isTurmaLotada(TurmaEntity turma) {
        int capacidade = turma.getCapacidade() == null ? 0 : turma.getCapacidade();
        long vagasOcupadas = matriculaJpaRepository.countByTurma_IdAndTurma_Escola_IdAndStatus_CodigoNotIn(
                turma.getId(),
                turma.getEscola().getId(),
                STATUS_NAO_OCUPAM_VAGA);
        return capacidade <= vagasOcupadas;
    }

    private long contarProfessoresAlocados(UUID escolaId) {
        return professorTurmaDisciplinaJpaRepository.findAll().stream()
                .filter(alocacao -> escolaId.equals(alocacao.getTurmaDisciplina().getTurma().getEscola().getId()))
                .filter(alocacao -> !Boolean.FALSE.equals(alocacao.getAtivo()))
                .map(ProfessorTurmaDisciplinaEntity::getProfessor)
                .map(professor -> professor.getId())
                .distinct()
                .count();
    }

    private long contarAulasRealizadas(UUID escolaId) {
        return aulaJpaRepository.findAllByProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(escolaId).stream()
                .filter(aula -> Boolean.TRUE.equals(aula.getRealizada()))
                .count();
    }

    private long contarAvaliacoesComNotasPendentes(UUID escolaId) {
        return avaliacaoJpaRepository.findAllByProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(escolaId).stream()
                .map(AvaliacaoEntity::getId)
                .filter(avaliacaoId -> notaAlunoJpaRepository
                        .findByAvaliacao_IdAndAvaliacao_ProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(
                                avaliacaoId,
                                escolaId)
                        .isEmpty())
                .count();
    }
}
