package br.com.escola.dashboard.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.aluno.adapter.out.persistence.repository.AlunoJpaRepository;
import br.com.escola.avaliacao.adapter.out.persistence.entity.AvaliacaoEntity;
import br.com.escola.avaliacao.adapter.out.persistence.repository.AvaliacaoJpaRepository;
import br.com.escola.avaliacao.adapter.out.persistence.repository.NotaAlunoJpaRepository;
import br.com.escola.catalogo.adapter.out.persistence.entity.TurmaEntity;
import br.com.escola.catalogo.adapter.out.persistence.repository.TurmaJpaRepository;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardAcademicoResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardDiretorResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardSecretariaResponse;
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

    private final DashboardAcademicoService dashboardAcademicoService;
    private final DashboardSecretariaService dashboardSecretariaService;
    private final AlunoJpaRepository alunoJpaRepository;
    private final TurmaJpaRepository turmaJpaRepository;
    private final MatriculaJpaRepository matriculaJpaRepository;
    private final ProfessorTurmaDisciplinaJpaRepository professorTurmaDisciplinaJpaRepository;
    private final AulaJpaRepository aulaJpaRepository;
    private final AvaliacaoJpaRepository avaliacaoJpaRepository;
    private final NotaAlunoJpaRepository notaAlunoJpaRepository;

    public DashboardDiretorService(
            DashboardAcademicoService dashboardAcademicoService,
            DashboardSecretariaService dashboardSecretariaService,
            AlunoJpaRepository alunoJpaRepository,
            TurmaJpaRepository turmaJpaRepository,
            MatriculaJpaRepository matriculaJpaRepository,
            ProfessorTurmaDisciplinaJpaRepository professorTurmaDisciplinaJpaRepository,
            AulaJpaRepository aulaJpaRepository,
            AvaliacaoJpaRepository avaliacaoJpaRepository,
            NotaAlunoJpaRepository notaAlunoJpaRepository) {
        this.dashboardAcademicoService = dashboardAcademicoService;
        this.dashboardSecretariaService = dashboardSecretariaService;
        this.alunoJpaRepository = alunoJpaRepository;
        this.turmaJpaRepository = turmaJpaRepository;
        this.matriculaJpaRepository = matriculaJpaRepository;
        this.professorTurmaDisciplinaJpaRepository = professorTurmaDisciplinaJpaRepository;
        this.aulaJpaRepository = aulaJpaRepository;
        this.avaliacaoJpaRepository = avaliacaoJpaRepository;
        this.notaAlunoJpaRepository = notaAlunoJpaRepository;
    }

    @Transactional(readOnly = true)
    public DashboardDiretorResponse consultar() {
        DashboardAcademicoResponse academico = dashboardAcademicoService.consultar();
        DashboardSecretariaResponse secretaria = dashboardSecretariaService.consultar();

        return new DashboardDiretorResponse(
                academico.totalMatriculas(),
                contarMatriculasPendentes(secretaria),
                academico.matriculasConcluidas(),
                academico.matriculasEfetivadas(),
                academico.matriculasAptasRematricula(),
                alunoJpaRepository.countByAtivoTrue(),
                alunoJpaRepository.countByAtivoFalse(),
                contarTurmasAtivas(),
                contarTurmasLotadas(),
                contarProfessoresAlocados(),
                contarAulasRealizadas(),
                avaliacaoJpaRepository.count(),
                contarAvaliacoesComNotasPendentes(),
                academico.boletinsFechados(),
                academico.historicosInternosGerados(),
                secretaria.transferencias(),
                secretaria.solicitacoesExclusaoPendentes(),
                secretaria.matriculasComDocumentosPendentes(),
                academico.matriculasPorStatus(),
                academico.turmasComVagas());
    }

    private long contarMatriculasPendentes(DashboardSecretariaResponse secretaria) {
        return secretaria.matriculasSolicitadas()
                + secretaria.matriculasEmAndamento()
                + secretaria.matriculasAguardandoDocumentos()
                + secretaria.matriculasAguardandoHistoricoEscolar();
    }

    private long contarTurmasAtivas() {
        return turmaJpaRepository.findAll().stream()
                .filter(turma -> !Boolean.FALSE.equals(turma.getAtivo()))
                .count();
    }

    private long contarTurmasLotadas() {
        return turmaJpaRepository.findAll().stream()
                .filter(turma -> !Boolean.FALSE.equals(turma.getAtivo()))
                .filter(this::isTurmaLotada)
                .count();
    }

    private boolean isTurmaLotada(TurmaEntity turma) {
        int capacidade = turma.getCapacidade() == null ? 0 : turma.getCapacidade();
        long vagasOcupadas = matriculaJpaRepository.countByTurma_IdAndStatus_CodigoNotIn(
                turma.getId(),
                STATUS_NAO_OCUPAM_VAGA);
        return capacidade <= vagasOcupadas;
    }

    private long contarProfessoresAlocados() {
        return professorTurmaDisciplinaJpaRepository.findAll().stream()
                .filter(alocacao -> !Boolean.FALSE.equals(alocacao.getAtivo()))
                .map(ProfessorTurmaDisciplinaEntity::getProfessor)
                .map(professor -> professor.getId())
                .distinct()
                .count();
    }

    private long contarAulasRealizadas() {
        return aulaJpaRepository.findAll().stream()
                .filter(aula -> Boolean.TRUE.equals(aula.getRealizada()))
                .count();
    }

    private long contarAvaliacoesComNotasPendentes() {
        return avaliacaoJpaRepository.findAll().stream()
                .map(AvaliacaoEntity::getId)
                .filter(avaliacaoId -> notaAlunoJpaRepository.findByAvaliacaoId(avaliacaoId).isEmpty())
                .count();
    }
}
