package br.com.escola.dashboard.application.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.catalogo.adapter.out.persistence.entity.TurmaEntity;
import br.com.escola.catalogo.adapter.out.persistence.repository.TurmaJpaRepository;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardAcademicoResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardMatriculaStatusResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardTurmaVagaResponse;
import br.com.escola.historico.adapter.out.persistence.repository.BoletimJpaRepository;
import br.com.escola.historico.adapter.out.persistence.repository.HistoricoEscolarJpaRepository;
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

    public DashboardAcademicoService(
            MatriculaJpaRepository matriculaJpaRepository,
            TurmaJpaRepository turmaJpaRepository,
            BoletimJpaRepository boletimJpaRepository,
            HistoricoEscolarJpaRepository historicoEscolarJpaRepository) {
        this.matriculaJpaRepository = matriculaJpaRepository;
        this.turmaJpaRepository = turmaJpaRepository;
        this.boletimJpaRepository = boletimJpaRepository;
        this.historicoEscolarJpaRepository = historicoEscolarJpaRepository;
    }

    @Transactional(readOnly = true)
    public DashboardAcademicoResponse consultar() {
        long matriculasConcluidas = countMatriculasPorStatus(MatriculaStatus.CONCLUIDA);

        return new DashboardAcademicoResponse(
                matriculaJpaRepository.count(),
                countMatriculasPorStatus(MatriculaStatus.AGUARDANDO_DOCUMENTOS),
                matriculasConcluidas,
                countMatriculasPorStatus(MatriculaStatus.EFETIVADA),
                matriculasConcluidas,
                boletimJpaRepository.count(),
                historicoEscolarJpaRepository.countByOrigemIgnoreCase("INTERNO"),
                boletimJpaRepository.countBoletinsAprovados(),
                boletimJpaRepository.countBoletinsReprovados(),
                consultarMatriculasPorStatus(),
                consultarTurmasComVagas());
    }

    private long countMatriculasPorStatus(MatriculaStatus status) {
        return matriculaJpaRepository.countByStatus_CodigoIgnoreCase(status.name());
    }

    private List<DashboardMatriculaStatusResponse> consultarMatriculasPorStatus() {
        return matriculaJpaRepository.countMatriculasPorStatus().stream()
                .map(projection -> new DashboardMatriculaStatusResponse(
                        projection.getStatus(),
                        projection.getTotal()))
                .toList();
    }

    private List<DashboardTurmaVagaResponse> consultarTurmasComVagas() {
        return turmaJpaRepository.findAll().stream()
                .filter(turma -> !Boolean.FALSE.equals(turma.getAtivo()))
                .map(this::toTurmaVagaResponse)
                .filter(turma -> turma.vagasDisponiveis() > 0)
                .sorted(Comparator.comparing(DashboardTurmaVagaResponse::turmaNome))
                .toList();
    }

    private DashboardTurmaVagaResponse toTurmaVagaResponse(TurmaEntity turma) {
        int capacidade = turma.getCapacidade() == null ? 0 : turma.getCapacidade();
        long vagasOcupadas = matriculaJpaRepository.countByTurma_IdAndStatus_CodigoNotIn(
                turma.getId(),
                STATUS_NAO_OCUPAM_VAGA);
        return new DashboardTurmaVagaResponse(
                turma.getId(),
                turma.getNome(),
                capacidade,
                vagasOcupadas,
                capacidade - vagasOcupadas);
    }
}
