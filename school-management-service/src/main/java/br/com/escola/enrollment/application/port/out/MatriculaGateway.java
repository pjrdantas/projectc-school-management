package br.com.escola.enrollment.application.port.out;

import java.util.UUID;

import java.util.List;
import java.util.Optional;

import br.com.escola.enrollment.application.dto.MatriculaFiltro;
import br.com.escola.enrollment.application.dto.MatriculaHistoricoOutput;
import br.com.escola.enrollment.application.dto.MatriculaOutput;
import br.com.escola.enrollment.domain.MatriculaStatus;
import br.com.escola.enrollment.domain.MatriculaTipo;

public interface MatriculaGateway {

    MatriculaOutput save(
            UUID alunoId,
            UUID turmaId,
            UUID periodoLetivoId,
            MatriculaStatus status,
            MatriculaTipo tipoMatricula,
            String observacao);

    List<MatriculaOutput> findByFiltro(MatriculaFiltro filtro, MatriculaStatus status);

    long countMatriculasQueOcupamVagaByTurmaId(UUID turmaId);

    boolean existsByAlunoIdAndPeriodoLetivoId(UUID alunoId, UUID periodoLetivoId);

    Optional<MatriculaHistoricoOutput> findHistoricoAnteriorMaisRecente(UUID alunoId, UUID periodoLetivoId);

    MatriculaOutput updateStatus(UUID id, MatriculaStatus status);

    MatriculaOutput updateStatus(UUID id, MatriculaStatus status, String justificativa);

    boolean existsById(UUID id);

    void deleteById(UUID id);
}
