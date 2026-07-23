package br.com.escola.identityaccessservice.application.port.out;

import java.util.UUID;

import br.com.escola.identityaccessservice.application.model.ContextoSessaoAutenticada;

public interface SessaoAutenticadaPort {

    ContextoSessaoAutenticada consultar(String accessToken);

    void atualizarEscolaAtiva(UUID sessaoId, UUID escolaId);
}

