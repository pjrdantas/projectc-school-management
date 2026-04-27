package br.com.escola.studentmanagement.application.usecase;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import br.com.escola.studentmanagement.application.dto.AlunoOutput;
import br.com.escola.studentmanagement.application.port.out.AlunoQueryGateway;
import br.com.escola.studentmanagement.domain.exception.AlunoNaoEncontradoException;

@Service
public class BuscarAlunoPorIdUseCase {

    private final AlunoQueryGateway alunoQueryGateway;

    public BuscarAlunoPorIdUseCase(AlunoQueryGateway alunoQueryGateway) {
        this.alunoQueryGateway = alunoQueryGateway;
    }

    public AlunoOutput executar(@NonNull Long id) {
        return alunoQueryGateway.findById(id).orElseThrow(() -> new AlunoNaoEncontradoException(id));
    }
}
