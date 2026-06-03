package br.com.escola.catalogo.application.usecase;

import org.springframework.stereotype.Service;

import br.com.escola.catalogo.application.dto.TurnoInput;
import br.com.escola.catalogo.application.dto.TurnoOutput;
import br.com.escola.catalogo.application.port.out.TurnoGateway;

@Service
public class CriarTurnoUseCase {

    private final TurnoGateway turnoGateway;

    public CriarTurnoUseCase(TurnoGateway turnoGateway) {
        this.turnoGateway = turnoGateway;
    }

    public TurnoOutput executar(TurnoInput input) {
        return turnoGateway.save(input);
    }
}
