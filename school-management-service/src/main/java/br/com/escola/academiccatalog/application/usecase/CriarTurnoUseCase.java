package br.com.escola.academiccatalog.application.usecase;

import org.springframework.stereotype.Service;

import br.com.escola.academiccatalog.application.dto.TurnoInput;
import br.com.escola.academiccatalog.application.dto.TurnoOutput;
import br.com.escola.academiccatalog.application.port.out.TurnoGateway;

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
