package br.com.escola.identityaccessservice.application.port.out;

import java.util.List;
import java.util.UUID;

import br.com.escola.identityaccessservice.application.context.InternalRequestContext;
import br.com.escola.identityaccessservice.application.dto.AuthContextResponse;
import br.com.escola.identityaccessservice.application.dto.EscolaSessaoResponse;

public interface SessaoAutenticadaPort {

    List<EscolaSessaoResponse> listarEscolasDisponiveis(
            String accessToken,
            InternalRequestContext context);

    AuthContextResponse selecionarEscolaAtiva(
            String accessToken,
            InternalRequestContext context,
            UUID escolaId);
}

