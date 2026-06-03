package br.com.escola.catalogo.application.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.catalogo.application.dto.TurnoInput;
import br.com.escola.catalogo.application.dto.TurnoOutput;
import br.com.escola.catalogo.application.port.out.TurnoGateway;
import br.com.escola.catalogo.domain.exception.TurnoNaoEncontradoException;

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
