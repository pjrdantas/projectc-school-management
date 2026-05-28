package br.com.escola.academiccatalog.application.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.academiccatalog.application.dto.TurnoInput;
import br.com.escola.academiccatalog.application.dto.TurnoOutput;
import br.com.escola.academiccatalog.application.port.out.TurnoGateway;
import br.com.escola.academiccatalog.domain.exception.TurnoNaoEncontradoException;

@Service
public class AtualizarTurnoUseCase {

    private final TurnoGateway turnoGateway;

    public AtualizarTurnoUseCase(TurnoGateway turnoGateway) {
        this.turnoGateway = turnoGateway;
    }

    public TurnoOutput executar(UUID id, TurnoInput input) {
        if (!turnoGateway.existsById(id)) {
            throw new TurnoNaoEncontradoException(id);
        }
        return turnoGateway.update(id, input);
    }
}
