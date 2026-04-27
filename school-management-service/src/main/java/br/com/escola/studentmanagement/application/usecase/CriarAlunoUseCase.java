package br.com.escola.studentmanagement.application.usecase;

import org.springframework.stereotype.Service;

import br.com.escola.studentmanagement.application.dto.AlunoInput;
import br.com.escola.studentmanagement.application.dto.AlunoOutput;
import br.com.escola.studentmanagement.application.port.out.AlunoCommandGateway;
import br.com.escola.studentmanagement.domain.exception.AlunoJaCadastradoException;

@Service
public class CriarAlunoUseCase {

    private final AlunoCommandGateway alunoCommandGateway;

    public CriarAlunoUseCase(AlunoCommandGateway alunoCommandGateway) {
        this.alunoCommandGateway = alunoCommandGateway;
    }

    public AlunoOutput executar(AlunoInput input) {
        if (alunoCommandGateway.existsByCpf(input.cpf())) {
            throw new AlunoJaCadastradoException();
        }
        return alunoCommandGateway.save(input);
    }
}
