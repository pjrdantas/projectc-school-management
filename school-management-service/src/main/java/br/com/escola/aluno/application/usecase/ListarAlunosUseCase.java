package br.com.escola.aluno.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.aluno.application.dto.AlunoOutput;
import br.com.escola.aluno.application.port.out.AlunoQueryGateway;

@Service
public class ListarAlunosUseCase {

    private final AlunoQueryGateway alunoQueryGateway;

    public ListarAlunosUseCase(AlunoQueryGateway alunoQueryGateway) {
        this.alunoQueryGateway = alunoQueryGateway;
    }

    public List<AlunoOutput> executar() {
        return alunoQueryGateway.findAll();
    }
}
