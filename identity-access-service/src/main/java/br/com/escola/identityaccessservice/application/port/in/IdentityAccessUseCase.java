package br.com.escola.identityaccessservice.application.port.in;

import java.util.List;

import br.com.escola.identityaccessservice.application.context.InternalRequestContext;
import br.com.escola.identityaccessservice.application.dto.AuthContextResponse;
import br.com.escola.identityaccessservice.application.dto.EscolaSessaoResponse;

public interface IdentityAccessUseCase {

    AuthContextResponse consultarContextoAtual(
            String authorization,
            InternalRequestContext context);

    List<EscolaSessaoResponse> listarEscolasDisponiveis(
            String authorization,
            InternalRequestContext context);

    AuthContextResponse selecionarEscolaAtiva(
            String authorization,
            InternalRequestContext context,
            java.util.UUID escolaId);
}
