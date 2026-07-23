package br.com.escola.responsiblesservice.application.port.in;

import java.util.UUID;

import br.com.escola.responsiblesservice.application.context.InternalRequestContext;
import br.com.escola.responsiblesservice.application.dto.VincularAlunoResponsavelCommand;

public interface AlunoResponsavelWriteUseCase {

    void vincular(UUID alunoId, VincularAlunoResponsavelCommand command, InternalRequestContext context);

    void desvincular(UUID alunoId, UUID responsavelId, InternalRequestContext context);
}
