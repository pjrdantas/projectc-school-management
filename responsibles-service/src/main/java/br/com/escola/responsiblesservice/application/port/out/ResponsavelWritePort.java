package br.com.escola.responsiblesservice.application.port.out;

import java.util.UUID;
import java.util.Optional;

import br.com.escola.responsiblesservice.application.dto.AtualizarResponsavelCommand;
import br.com.escola.responsiblesservice.application.dto.CadastrarResponsavelCommand;
import br.com.escola.responsiblesservice.application.dto.ResponsavelReadModelResponse;
import br.com.escola.responsiblesservice.application.dto.ExclusaoResponsavelResultado;

public interface ResponsavelWritePort {

    ResponsavelReadModelResponse criar(CadastrarResponsavelCommand command, UUID escolaId);

    Optional<ResponsavelReadModelResponse> atualizar(
            UUID responsavelId,
            AtualizarResponsavelCommand command,
            UUID escolaId);

    ExclusaoResponsavelResultado excluir(UUID responsavelId, UUID escolaId);
}
