package br.com.escola.pedagogicalservice.application.port.in;

import java.util.UUID;

import br.com.escola.pedagogicalservice.application.context.InternalRequestContext;
import br.com.escola.pedagogicalservice.application.dto.BoletimResponse;

public interface BoletimQueryUseCase {

    BoletimResponse consultarBoletimPorMatricula(
            String authorization,
            InternalRequestContext context,
            UUID matriculaId);
}
