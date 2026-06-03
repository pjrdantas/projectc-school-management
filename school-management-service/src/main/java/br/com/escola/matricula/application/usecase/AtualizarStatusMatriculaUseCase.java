package br.com.escola.matricula.application.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.matricula.application.dto.MatriculaOutput;
import br.com.escola.matricula.application.port.out.MatriculaGateway;
import br.com.escola.matricula.domain.MatriculaStatus;
import br.com.escola.matricula.domain.exception.MatriculaStatusInvalidoException;

@Service
public class AtualizarStatusMatriculaUseCase {

    private final MatriculaGateway matriculaGateway;

    public AtualizarStatusMatriculaUseCase(MatriculaGateway matriculaGateway) {
        this.matriculaGateway = matriculaGateway;
    }

    public MatriculaOutput executar(UUID id, String status) {
        return executar(id, status, null);
    }

    public MatriculaOutput executar(UUID id, String status, String justificativa) {
        return matriculaGateway.updateStatus(id, parseStatus(status), justificativa);
    }

    private MatriculaStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new MatriculaStatusInvalidoException(status);
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
