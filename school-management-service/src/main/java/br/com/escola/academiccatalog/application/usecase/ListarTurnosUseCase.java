package br.com.escola.academiccatalog.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.academiccatalog.application.dto.TurnoOutput;
import br.com.escola.academiccatalog.application.port.out.TurnoGateway;

@Service
public class ListarTurnosUseCase {

    private final TurnoGateway turnoGateway;

    public ListarTurnosUseCase(TurnoGateway turnoGateway) {
        this.turnoGateway = turnoGateway;
    }

    public List<TurnoOutput> executar() {
        return turnoGateway.findAll();
    }
}
