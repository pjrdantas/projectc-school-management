package br.com.escola.matricula.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.catalogo.adapter.out.persistence.entity.TurmaEntity;
import br.com.escola.catalogo.adapter.out.persistence.repository.TurmaJpaRepository;
import br.com.escola.institucional.application.service.EscolaTenantService;
import br.com.escola.matricula.adapter.out.persistence.entity.MatriculaEntity;
import br.com.escola.matricula.adapter.out.persistence.repository.MatriculaJpaRepository;
import br.com.escola.matricula.application.dto.internal.MatriculaRematriculaElegibilidadeResumo;
import br.com.escola.matricula.application.port.internal.MatriculaRematriculaPort;
import br.com.escola.matricula.domain.exception.MatriculaNaoEncontradaException;

@Service
public class MatriculaRematriculaService implements MatriculaRematriculaPort {

    private static final List<String> STATUS_NAO_OCUPAM_VAGA = List.of("CANCELADA", "INDEFERIDA", "TRANSFERIDO");

    private final MatriculaJpaRepository matriculaJpaRepository;
    private final TurmaJpaRepository turmaJpaRepository;
    private final EscolaTenantService escolaTenantService;

    public MatriculaRematriculaService(
            MatriculaJpaRepository matriculaJpaRepository,
            TurmaJpaRepository turmaJpaRepository,
            EscolaTenantService escolaTenantService) {
        this.matriculaJpaRepository = matriculaJpaRepository;
        this.turmaJpaRepository = turmaJpaRepository;
        this.escolaTenantService = escolaTenantService;
    }

    @Override
    @Transactional(readOnly = true)
    public MatriculaRematriculaElegibilidadeResumo consultarElegibilidade(
            UUID matriculaAnteriorId,
            UUID turmaDestinoId,
            UUID periodoLetivoDestinoId) {
        MatriculaEntity matriculaAnterior = matriculaJpaRepository.findByIdAndTurma_Escola_Id(
                        matriculaAnteriorId,
                        escolaId())
                .orElseThrow(() -> new MatriculaNaoEncontradaException(matriculaAnteriorId));
        List<String> motivos = new ArrayList<>();

        if (!"CONCLUIDA".equalsIgnoreCase(matriculaAnterior.getStatus().getCodigo())) {
            motivos.add("Matrícula base deve estar concluída para renovação");
        }

        TurmaEntity turmaDestino = null;
        if (turmaDestinoId != null) {
            turmaDestino = turmaJpaRepository.findByIdAndEscola_Id(turmaDestinoId, escolaId()).orElse(null);
            if (turmaDestino == null) {
                motivos.add("Turma de destino não encontrada");
            }
        }

        if (turmaDestino != null && periodoLetivoDestinoId != null
                && !turmaDestino.getPeriodoLetivo().getId().equals(periodoLetivoDestinoId)) {
            motivos.add("Turma de destino não pertence ao período letivo informado");
        }

        if (periodoLetivoDestinoId != null
                && matriculaJpaRepository.existsByAluno_IdAndAluno_Pessoa_Escola_IdAndPeriodoLetivo_Id(
                        matriculaAnterior.getAluno().getId(),
                        escolaId(),
                        periodoLetivoDestinoId)) {
            motivos.add("Aluno já possui matrícula no período letivo de destino");
        }

        if (turmaDestino != null) {
            Integer serieOrigem = matriculaAnterior.getTurma().getSerie().getOrdem();
            Integer serieDestino = turmaDestino.getSerie().getOrdem();
            if (serieOrigem == null || serieDestino == null || !serieDestino.equals(serieOrigem + 1)) {
                motivos.add("Turma de destino deve ser da série imediatamente posterior");
            }

            long matriculasQueOcupamVaga =
                    matriculaJpaRepository.countByTurma_IdAndTurma_Escola_IdAndStatus_CodigoNotIn(
                            turmaDestinoId,
                            escolaId(),
                            STATUS_NAO_OCUPAM_VAGA);
            if (matriculasQueOcupamVaga >= turmaDestino.getCapacidade()) {
                motivos.add("Turma de destino não possui vaga disponível");
            }
        }

        return new MatriculaRematriculaElegibilidadeResumo(
                matriculaAnterior.getId(),
                matriculaAnterior.getAluno().getId(),
                matriculaAnterior.getStatus().getCodigo(),
                matriculaAnterior.getTurma().getId(),
                matriculaAnterior.getTurma().getSerie().getId(),
                matriculaAnterior.getTurma().getSerie().getNome(),
                matriculaAnterior.getTurma().getSerie().getOrdem(),
                turmaDestino == null ? turmaDestinoId : turmaDestino.getId(),
                periodoLetivoDestinoId,
                turmaDestino == null ? null : turmaDestino.getSerie().getId(),
                turmaDestino == null ? null : turmaDestino.getSerie().getNome(),
                turmaDestino == null ? null : turmaDestino.getSerie().getOrdem(),
                motivos.isEmpty(),
                motivos);
    }

    private UUID escolaId() {
        return escolaTenantService.obterOuCriarEscolaPadrao().getId();
    }
}
