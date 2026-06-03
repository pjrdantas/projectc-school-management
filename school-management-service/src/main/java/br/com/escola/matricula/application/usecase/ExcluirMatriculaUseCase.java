package br.com.escola.matricula.application.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.matricula.application.port.out.MatriculaGateway;
import br.com.escola.matricula.domain.exception.MatriculaNaoEncontradaException;

@Service
public class ExcluirMatriculaUseCase {

    private final MatriculaGateway matriculaGateway;

    public ExcluirMatriculaUseCase(MatriculaGateway matriculaGateway) {
        this.matriculaGateway = matriculaGateway;
    }

    public void executar(UUID id) {
        if (!matriculaGateway.existsById(id)) {
            throw new MatriculaNaoEncontradaException(id);
        }

        matriculaGateway.deleteById(id);
    }
}
