package br.com.escola.catalogo.application.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.catalogo.application.dto.TurnoOutput;
import br.com.escola.catalogo.application.port.out.TurnoGateway;
import br.com.escola.catalogo.domain.exception.TurnoNaoEncontradoException;

@Service
public class BuscarTurnoPorIdUseCase {

    private final TurnoGateway turnoGateway;

    public BuscarTurnoPorIdUseCase(TurnoGateway turnoGateway) {
        this.turnoGateway = turnoGateway;
    }

    public TurnoOutput executar(UUID id) {
        return turnoGateway.findById(id).orElseThrow(() -> new TurnoNaoEncontradoException(id));
    }
}
