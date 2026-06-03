package br.com.escola.catalogo.application.usecase;

import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import br.com.escola.catalogo.application.dto.TurmaOutput;
import br.com.escola.catalogo.application.port.out.TurmaGateway;
import br.com.escola.catalogo.domain.exception.TurmaNaoEncontradaException;

@Service
public class BuscarTurmaPorIdUseCase {

    private final TurmaGateway turmaGateway;

    public BuscarTurmaPorIdUseCase(TurmaGateway turmaGateway) {
        this.turmaGateway = turmaGateway;
    }

    public TurmaOutput executar(@NonNull UUID id) {
        return turmaGateway.findById(id).orElseThrow(() -> new TurmaNaoEncontradaException(id));
    }
}
