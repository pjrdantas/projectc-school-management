package br.com.escola.academiccatalog.application.usecase;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import br.com.escola.academiccatalog.application.dto.TurmaOutput;
import br.com.escola.academiccatalog.application.port.out.TurmaGateway;
import br.com.escola.academiccatalog.domain.exception.TurmaNaoEncontradaException;

@Service
public class BuscarTurmaPorIdUseCase {

    private final TurmaGateway turmaGateway;

    public BuscarTurmaPorIdUseCase(TurmaGateway turmaGateway) {
        this.turmaGateway = turmaGateway;
    }

    public TurmaOutput executar(@NonNull Long id) {
        return turmaGateway.findById(id).orElseThrow(() -> new TurmaNaoEncontradaException(id));
    }
}
