package br.com.escola.dashboard.application.service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.catalogo.adapter.out.persistence.entity.TurmaEntity;
import br.com.escola.catalogo.adapter.out.persistence.repository.TurmaJpaRepository;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardAcademicoResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardMatriculaStatusResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardTurmaVagaResponse;
import br.com.escola.historico.adapter.out.persistence.repository.BoletimJpaRepository;
import br.com.escola.historico.adapter.out.persistence.repository.HistoricoEscolarJpaRepository;
import br.com.escola.institucional.application.service.EscolaTenantService;
import br.com.escola.matricula.adapter.out.persistence.repository.MatriculaJpaRepository;
import br.com.escola.matricula.domain.MatriculaStatus;

@Service
public class DashboardAcademicoService {

    private static final List<String> STATUS_NAO_OCUPAM_VAGA = List.of(
            MatriculaStatus.CANCELADA.name(),
            MatriculaStatus.INDEFERIDA.name(),
            MatriculaStatus.TRANSFERIDO.name());

    private final MatriculaJpaRepository matriculaJpaRepository;
    private final TurmaJpaRepository turmaJpaRepository;
    private final BoletimJpaRepository boletimJpaRepository;
    private final HistoricoEscolarJpaRepository historicoEscolarJpaRepository;
    private final EscolaTenantService escolaTenantService;

    public DashboardAcademicoService(
            MatriculaJpaRepository matriculaJpaRepository,
            TurmaJpaRepository turmaJpaRepository,
            BoletimJpaRepository boletimJpaRepository,
            HistoricoEscolarJpaRepository historicoEscolarJpaRepository,
            EscolaTenantService escolaTenantService) {
        this.matriculaJpaRepository = matriculaJpaRepository;
        this.turmaJpaRepository = turmaJpaRepository;
        this.boletimJpaRepository = boletimJpaRepository;
        this.historicoEscolarJpaRepository = historicoEscolarJpaRepository;
        this.escolaTenantService = escolaTenantService;
    }

    @Transactional(readOnly = true)
    public DashboardAcademicoResponse consultar() {
        UUID escolaId = escolaTenantService.obterOuCriarEscolaPadrao().getId();
        long matriculasConcluidas = countMatriculasPorStatus(escolaId, MatriculaStatus.CONCLUIDA);

        return new DashboardAcademicoResponse(
                matriculaJpaRepository.countByTurma_Escola_Id(escolaId),
                countMatriculasPorStatus(escolaId, MatriculaStatus.AGUARDANDO_DOCUMENTOS),
                matriculasConcluidas,
                countMatriculasPorStatus(escolaId, MatriculaStatus.EFETIVADA),
                matriculasConcluidas,
                boletimJpaRepository.countByMatricula_Turma_Escola_Id(escolaId),
                historicoEscolarJpaRepository.countByOrigemIgnoreCaseAndAluno_Pessoa_Escola_Id("INTERNO", escolaId),
                boletimJpaRepository.countBoletinsAprovadosByEscolaId(escolaId),
                boletimJpaRepository.countBoletinsReprovadosByEscolaId(escolaId),
                consultarMatriculasPorStatus(escolaId),
                consultarTurmasComVagas(escolaId));
    }

    private long countMatriculasPorStatus(UUID escolaId, MatriculaStatus status) {
        return matriculaJpaRepository.countByTurma_Escola_IdAndStatus_CodigoIgnoreCase(escolaId, status.name());
    }

    private List<DashboardMatriculaStatusResponse> consultarMatriculasPorStatus(UUID escolaId) {
        return matriculaJpaRepository.countMatriculasPorStatusByEscolaId(escolaId).stream()
                .map(projection -> new DashboardMatriculaStatusResponse(
                        projection.getStatus(),
                        projection.getTotal()))
                .toList();
    }

    private List<DashboardTurmaVagaResponse> consultarTurmasComVagas(UUID escolaId) {
        return turmaJpaRepository.findAllByEscola_Id(escolaId).stream()
                .filter(turma -> !Boolean.FALSE.equals(turma.getAtivo()))
                .map(this::toTurmaVagaResponse)
                .filter(turma -> turma.vagasDisponiveis() > 0)
                .sorted(Comparator.comparing(DashboardTurmaVagaResponse::turmaNome))
                .toList();
    }

    private DashboardTurmaVagaResponse toTurmaVagaResponse(TurmaEntity turma) {
        int capacidade = turma.getCapacidade() == null ? 0 : turma.getCapacidade();
        long vagasOcupadas = matriculaJpaRepository.countByTurma_IdAndTurma_Escola_IdAndStatus_CodigoNotIn(
                turma.getId(),
                turma.getEscola().getId(),
                STATUS_NAO_OCUPAM_VAGA);
        return new DashboardTurmaVagaResponse(
                turma.getId(),
                turma.getNome(),
                capacidade,
                vagasOcupadas,
                capacidade - vagasOcupadas);
    }
}
