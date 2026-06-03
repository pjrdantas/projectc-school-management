package br.com.escola.aluno.application.usecase;

import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import br.com.escola.aluno.application.port.out.AlunoCommandGateway;
import br.com.escola.aluno.application.port.out.AlunoQueryGateway;
import br.com.escola.aluno.domain.exception.AlunoNaoEncontradoException;

@Service
public class ExcluirAlunoUseCase {

    private final AlunoQueryGateway alunoQueryGateway;
    private final AlunoCommandGateway alunoCommandGateway;

    public ExcluirAlunoUseCase(AlunoQueryGateway alunoQueryGateway, AlunoCommandGateway alunoCommandGateway) {
        this.alunoQueryGateway = alunoQueryGateway;
        this.alunoCommandGateway = alunoCommandGateway;
    }

    public void executar(@NonNull UUID id) {
        if (!alunoQueryGateway.existsById(id)) {
            throw new AlunoNaoEncontradoException(id);
        }

        alunoCommandGateway.deleteById(id);
    }
}
