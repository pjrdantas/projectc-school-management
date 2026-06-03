package br.com.escola.catalogo.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.catalogo.application.dto.TurmaOutput;
import br.com.escola.catalogo.application.port.out.TurmaGateway;

@Service
public class ListarTurmasUseCase {

    private final TurmaGateway turmaGateway;

    public ListarTurmasUseCase(TurmaGateway turmaGateway) {
        this.turmaGateway = turmaGateway;
    }

    public List<TurmaOutput> executar() {
        return turmaGateway.findAll();
    }
}
