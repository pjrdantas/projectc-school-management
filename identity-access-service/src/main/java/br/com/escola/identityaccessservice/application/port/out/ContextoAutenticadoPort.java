package br.com.escola.identityaccessservice.application.port.out;

import br.com.escola.identityaccessservice.application.dto.AuthContextResponse;

public interface ContextoAutenticadoPort {

    AuthContextResponse consultarContextoAtual(String accessToken);
}
