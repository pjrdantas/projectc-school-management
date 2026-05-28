package br.com.escola.academiccatalog.application.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.academiccatalog.application.dto.SerieOutput;
import br.com.escola.academiccatalog.application.port.out.SerieGateway;
import br.com.escola.academiccatalog.domain.exception.SerieNaoEncontradaException;

@Service
public class BuscarSeriePorIdUseCase {

    private final SerieGateway serieGateway;

    public BuscarSeriePorIdUseCase(SerieGateway serieGateway) {
        this.serieGateway = serieGateway;
    }

    public SerieOutput executar(UUID id) {
        return serieGateway.findById(id).orElseThrow(() -> new SerieNaoEncontradaException(id));
    }
}
