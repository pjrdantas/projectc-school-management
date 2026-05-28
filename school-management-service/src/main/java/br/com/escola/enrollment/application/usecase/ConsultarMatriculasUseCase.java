package br.com.escola.enrollment.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.enrollment.application.dto.MatriculaFiltro;
import br.com.escola.enrollment.application.dto.MatriculaOutput;
import br.com.escola.enrollment.application.port.out.MatriculaGateway;
import br.com.escola.enrollment.domain.MatriculaStatus;
import br.com.escola.enrollment.domain.exception.MatriculaStatusInvalidoException;

@Service
public class ConsultarMatriculasUseCase {

    private final MatriculaGateway matriculaGateway;

    public ConsultarMatriculasUseCase(MatriculaGateway matriculaGateway) {
        this.matriculaGateway = matriculaGateway;
    }

    public List<MatriculaOutput> executar(MatriculaFiltro filtro) {
        String status = filtro.status();
        MatriculaStatus matriculaStatus = parseStatus(status);
        return matriculaGateway.findByFiltro(filtro, matriculaStatus);
    }

    private MatriculaStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        String normalized = status.trim().toUpperCase();
        if (normalized.equals("ATIVA")) {
            return MatriculaStatus.EFETIVADA;
        }
        if (normalized.equals("TRANCADA")) {
            return MatriculaStatus.CANCELADA;
        }

        try {
            return MatriculaStatus.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new MatriculaStatusInvalidoException(status);
        }
    }
}
