package br.com.escola.pedagogicalservice.application.port.out;

import java.util.UUID;

import br.com.escola.pedagogicalservice.application.context.InternalRequestContext;
import br.com.escola.pedagogicalservice.application.dto.BoletimResponse;

public interface BoletimReadPort {

    BoletimResponse consultarBoletimPorMatricula(
            String authorization,
            InternalRequestContext context,
            UUID matriculaId);
}
