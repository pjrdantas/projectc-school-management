package br.com.escola.aluno.application.usecase;

import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import br.com.escola.aluno.application.dto.AlunoOutput;
import br.com.escola.aluno.application.port.out.AlunoQueryGateway;
import br.com.escola.aluno.domain.exception.AlunoNaoEncontradoException;

@Service
public class BuscarAlunoPorIdUseCase {

    private final AlunoQueryGateway alunoQueryGateway;

    public BuscarAlunoPorIdUseCase(AlunoQueryGateway alunoQueryGateway) {
        this.alunoQueryGateway = alunoQueryGateway;
    }

    public AlunoOutput executar(@NonNull UUID id) {
        return alunoQueryGateway.findById(id).orElseThrow(() -> new AlunoNaoEncontradoException(id));
    }
}
