package br.com.escola.responsiblesservice.application.port.in;

import java.util.UUID;

import br.com.escola.responsiblesservice.application.context.InternalRequestContext;
import br.com.escola.responsiblesservice.application.dto.AtualizarResponsavelCommand;
import br.com.escola.responsiblesservice.application.dto.CadastrarResponsavelCommand;
import br.com.escola.responsiblesservice.application.dto.ResponsavelReadModelResponse;

public interface ResponsavelWriteUseCase {

    ResponsavelReadModelResponse criar(CadastrarResponsavelCommand command, InternalRequestContext context);

    ResponsavelReadModelResponse atualizar(
            UUID responsavelId,
            AtualizarResponsavelCommand command,
            InternalRequestContext context);

    void excluir(UUID responsavelId, InternalRequestContext context);
}
