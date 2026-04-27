package br.com.escola.studentmanagement.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.studentmanagement.application.dto.AlunoOutput;
import br.com.escola.studentmanagement.application.port.out.AlunoQueryGateway;

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
