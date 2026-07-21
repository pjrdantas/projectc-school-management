package br.com.escola.responsiblesservice.application.port.out;

import java.util.UUID;

import br.com.escola.responsiblesservice.application.dto.CriacaoAlunoResponsavelResultado;
import br.com.escola.responsiblesservice.application.dto.DesvinculoAlunoResponsavelResultado;
import br.com.escola.responsiblesservice.application.dto.VincularAlunoResponsavelCommand;

public interface AlunoResponsavelWritePort {

    CriacaoAlunoResponsavelResultado vincular(
            UUID alunoId,
            VincularAlunoResponsavelCommand command,
            UUID escolaId);

    DesvinculoAlunoResponsavelResultado desvincular(UUID alunoId, UUID responsavelId, UUID escolaId);
}
