package br.com.escola.studentmanagement.application.usecase;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import br.com.escola.studentmanagement.application.dto.AlunoInput;
import br.com.escola.studentmanagement.application.dto.AlunoOutput;
import br.com.escola.studentmanagement.application.port.out.AlunoCommandGateway;
import br.com.escola.studentmanagement.application.port.out.AlunoQueryGateway;
import br.com.escola.studentmanagement.domain.exception.AlunoJaCadastradoException;
import br.com.escola.studentmanagement.domain.exception.AlunoNaoEncontradoException;

@Service
public class AtualizarAlunoUseCase {

    private final AlunoCommandGateway alunoCommandGateway;
    private final AlunoQueryGateway alunoQueryGateway;

    public AtualizarAlunoUseCase(AlunoCommandGateway alunoCommandGateway, AlunoQueryGateway alunoQueryGateway) {
        this.alunoCommandGateway = alunoCommandGateway;
        this.alunoQueryGateway = alunoQueryGateway;
    }

    public AlunoOutput executar(@NonNull Long id, AlunoInput input) {
        if (!alunoQueryGateway.existsById(id)) {
            throw new AlunoNaoEncontradoException(id);
        }

        if (alunoCommandGateway.existsByCpfAndIdNot(input.cpf(), id)) {
            throw new AlunoJaCadastradoException();
        }

        return alunoCommandGateway.update(id, input);
    }
}
