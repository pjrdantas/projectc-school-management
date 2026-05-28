package br.com.escola.academiccatalog.application.usecase;

import org.springframework.stereotype.Service;

import br.com.escola.academiccatalog.application.dto.SerieInput;
import br.com.escola.academiccatalog.application.dto.SerieOutput;
import br.com.escola.academiccatalog.application.port.out.SerieGateway;

@Service
public class CriarSerieUseCase {

    private final SerieGateway serieGateway;

    public CriarSerieUseCase(SerieGateway serieGateway) {
        this.serieGateway = serieGateway;
    }

    public SerieOutput executar(SerieInput input) {
        return serieGateway.save(input);
    }
}
