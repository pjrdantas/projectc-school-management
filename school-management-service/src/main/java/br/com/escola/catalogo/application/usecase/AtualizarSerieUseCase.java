package br.com.escola.catalogo.application.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.catalogo.application.dto.SerieInput;
import br.com.escola.catalogo.application.dto.SerieOutput;
import br.com.escola.catalogo.application.port.out.SerieGateway;
import br.com.escola.catalogo.domain.exception.SerieNaoEncontradaException;

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
