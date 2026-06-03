package br.com.escola.catalogo.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.catalogo.application.dto.SerieOutput;
import br.com.escola.catalogo.application.port.out.SerieGateway;

@Service
public class ListarSeriesUseCase {

    private final SerieGateway serieGateway;

    public ListarSeriesUseCase(SerieGateway serieGateway) {
        this.serieGateway = serieGateway;
    }

    public List<SerieOutput> executar() {
        return serieGateway.findAll();
    }
}
