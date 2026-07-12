package br.com.escola.dashboard.application.service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.catalogo.application.dto.internal.TurmaResumo;
import br.com.escola.catalogo.application.port.internal.CatalogoAcademicoPort;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardAcademicoResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardMatriculaStatusResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardTurmaVagaResponse;
import br.com.escola.dashboard.application.dto.internal.DashboardAcademicoResumo;
import br.com.escola.dashboard.application.dto.internal.DashboardMatriculaStatusResumo;
import br.com.escola.dashboard.application.dto.internal.DashboardTurmaVagaResumo;
import br.com.escola.dashboard.application.port.internal.DashboardAcademicoPort;
import br.com.escola.historico.adapter.out.persistence.repository.BoletimJpaRepository;
import br.com.escola.historico.adapter.out.persistence.repository.HistoricoEscolarJpaRepository;
import br.com.escola.institucional.application.dto.EscolaContexto;
import br.com.escola.institucional.application.port.EscolaContextoPort;
import br.com.escola.matricula.adapter.out.persistence.repository.MatriculaJpaRepository;
import br.com.escola.matricula.domain.MatriculaStatus;

@Service
public class DashboardAcademicoService implements DashboardAcademicoPort {

    private static final List<String> STATUS_NAO_OCUPAM_VAGA = List.of(
            MatriculaStatus.CANCELADA.name(),
            MatriculaStatus.INDEFERIDA.name(),
            MatriculaStatus.TRANSFERIDO.name());

    private final MatriculaJpaRepository matriculaJpaRepository;
    private final CatalogoAcademicoPort catalogoAcademicoPort;
    private final BoletimJpaRepository boletimJpaRepository;
    private final HistoricoEscolarJpaRepository historicoEscolarJpaRepository;
    private final EscolaContextoPort escolaContextoPort;

    public DashboardAcademicoService(
            MatriculaJpaRepository matriculaJpaRepository,
            CatalogoAcademicoPort catalogoAcademicoPort,
            BoletimJpaRepository boletimJpaRepository,
            HistoricoEscolarJpaRepository historicoEscolarJpaRepository,
            EscolaContextoPort escolaContextoPort) {
        this.matriculaJpaRepository = matriculaJpaRepository;
        this.catalogoAcademicoPort = catalogoAcademicoPort;
        this.boletimJpaRepository = boletimJpaRepository;
        this.historicoEscolarJpaRepository = historicoEscolarJpaRepository;
        this.escolaContextoPort = escolaContextoPort;
    }

    @Transactional(readOnly = true)
    public DashboardAcademicoResponse consultar() {
        return toResponse(consultarResumo());
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardAcademicoResumo consultarResumo() {
        EscolaContexto contexto = escolaContextoPort.obterContextoPadrao();
        UUID escolaId = contexto.escolaId();
        long matriculasConcluidas = countMatriculasPorStatus(escolaId, MatriculaStatus.CONCLUIDA);

        return new DashboardAcademicoResumo(
                contexto.escolaId(),
                contexto.escolaNome(),
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

    private List<DashboardMatriculaStatusResumo> consultarMatriculasPorStatus(UUID escolaId) {
        return matriculaJpaRepository.countMatriculasPorStatusByEscolaId(escolaId).stream()
                .map(projection -> new DashboardMatriculaStatusResumo(
                        projection.getStatus(),
                        projection.getTotal()))
                .toList();
    }

    private List<DashboardTurmaVagaResumo> consultarTurmasComVagas(UUID escolaId) {
        return catalogoAcademicoPort.listarTurmas(escolaId).stream()
                .filter(TurmaResumo::ativo)
                .map(this::toTurmaVagaResponse)
                .filter(turma -> turma.vagasDisponiveis() > 0)
                .sorted(Comparator.comparing(DashboardTurmaVagaResumo::turmaNome))
                .toList();
    }

    private DashboardTurmaVagaResumo toTurmaVagaResponse(TurmaResumo turma) {
        int capacidade = turma.capacidade() == null ? 0 : turma.capacidade();
        long vagasOcupadas = matriculaJpaRepository.countByTurma_IdAndTurma_Escola_IdAndStatus_CodigoNotIn(
                turma.id(),
                turma.escolaId(),
                STATUS_NAO_OCUPAM_VAGA);
        return new DashboardTurmaVagaResumo(
                turma.id(),
                turma.nome(),
                capacidade,
                vagasOcupadas,
                capacidade - vagasOcupadas);
    }

    private DashboardAcademicoResponse toResponse(DashboardAcademicoResumo resumo) {
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
}
