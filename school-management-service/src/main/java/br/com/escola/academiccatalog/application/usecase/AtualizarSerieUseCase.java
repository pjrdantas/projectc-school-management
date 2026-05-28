package br.com.escola.academiccatalog.application.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.academiccatalog.application.dto.SerieInput;
import br.com.escola.academiccatalog.application.dto.SerieOutput;
import br.com.escola.academiccatalog.application.port.out.SerieGateway;
import br.com.escola.academiccatalog.domain.exception.SerieNaoEncontradaException;

@Service
public class AtualizarSerieUseCase {

    private final SerieGateway serieGateway;

    public AtualizarSerieUseCase(SerieGateway serieGateway) {
        this.serieGateway = serieGateway;
    }

    public SerieOutput executar(UUID id, SerieInput input) {
        if (!serieGateway.existsById(id)) {
            throw new SerieNaoEncontradaException(id);
        }
        return serieGateway.update(id, input);
    }
}
