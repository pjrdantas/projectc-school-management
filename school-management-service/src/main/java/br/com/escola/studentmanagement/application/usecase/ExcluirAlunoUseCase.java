package br.com.escola.studentmanagement.application.usecase;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import br.com.escola.studentmanagement.application.port.out.AlunoCommandGateway;
import br.com.escola.studentmanagement.application.port.out.AlunoQueryGateway;
import br.com.escola.studentmanagement.domain.exception.AlunoNaoEncontradoException;

@Service
public class ExcluirAlunoUseCase {

    private final AlunoQueryGateway alunoQueryGateway;
    private final AlunoCommandGateway alunoCommandGateway;

    public ExcluirAlunoUseCase(AlunoQueryGateway alunoQueryGateway, AlunoCommandGateway alunoCommandGateway) {
        this.alunoQueryGateway = alunoQueryGateway;
        this.alunoCommandGateway = alunoCommandGateway;
    }

    public void executar(@NonNull Long id) {
        if (!alunoQueryGateway.existsById(id)) {
            throw new AlunoNaoEncontradoException(id);
        }

        alunoCommandGateway.deleteById(id);
    }
}
