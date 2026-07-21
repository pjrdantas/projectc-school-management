package br.com.escola.peopleservice.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.context.InternalRequestContext;
import br.com.escola.peopleservice.application.port.in.ExcluirAlunoUseCase;
import br.com.escola.peopleservice.application.port.out.AlunoExclusaoPort;

@Service
public class ExclusaoAlunoService implements ExcluirAlunoUseCase {

    private final AlunoExclusaoPort alunoExclusaoPort;

    public ExclusaoAlunoService(AlunoExclusaoPort alunoExclusaoPort) {
        this.alunoExclusaoPort = alunoExclusaoPort;
    }

    @Override
    public void excluir(UUID alunoId, InternalRequestContext context) {
        alunoExclusaoPort.excluir(alunoId, context.escolaId());
    }
}
