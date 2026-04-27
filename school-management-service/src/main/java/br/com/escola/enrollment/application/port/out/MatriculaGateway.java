package br.com.escola.enrollment.application.port.out;

import java.util.List;

import br.com.escola.enrollment.application.dto.MatriculaFiltro;
import br.com.escola.enrollment.application.dto.MatriculaOutput;
import br.com.escola.enrollment.domain.MatriculaStatus;

public interface MatriculaGateway {

    MatriculaOutput save(Long alunoId, Long turmaId, Long periodoLetivoId, MatriculaStatus status);

    List<MatriculaOutput> findByFiltro(MatriculaFiltro filtro, MatriculaStatus status);
}
