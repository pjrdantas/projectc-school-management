package br.com.escola.enrollment.application.port.out;

import java.util.UUID;

import java.util.List;

import br.com.escola.enrollment.application.dto.MatriculaFiltro;
import br.com.escola.enrollment.application.dto.MatriculaOutput;
import br.com.escola.enrollment.domain.MatriculaStatus;

public interface MatriculaGateway {

    MatriculaOutput save(UUID alunoId, UUID turmaId, UUID periodoLetivoId, MatriculaStatus status);

    List<MatriculaOutput> findByFiltro(MatriculaFiltro filtro, MatriculaStatus status);
}
