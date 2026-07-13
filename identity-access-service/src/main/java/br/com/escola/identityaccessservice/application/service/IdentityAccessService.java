package br.com.escola.identityaccessservice.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.identityaccessservice.application.context.InternalRequestContext;
import br.com.escola.identityaccessservice.application.dto.AuthContextResponse;
import br.com.escola.identityaccessservice.application.dto.EscolaSessaoResponse;
import br.com.escola.identityaccessservice.application.port.in.IdentityAccessUseCase;
import br.com.escola.identityaccessservice.application.port.out.IdentityAccessPort;

@Service
public class IdentityAccessService implements IdentityAccessUseCase {

    private final IdentityAccessPort identityAccessPort;

    public IdentityAccessService(IdentityAccessPort identityAccessPort) {
        this.identityAccessPort = identityAccessPort;
    }

    @Override
    public List<EscolaSessaoResponse> listarEscolasDisponiveis(
            String authorization,
            InternalRequestContext context) {
        return identityAccessPort.listarEscolasDisponiveis(authorization, context);
    }

    @Override
    public AuthContextResponse selecionarEscolaAtiva(
            String authorization,
            InternalRequestContext context,
            UUID escolaId) {
        return identityAccessPort.selecionarEscolaAtiva(authorization, context, escolaId);
    }
}
