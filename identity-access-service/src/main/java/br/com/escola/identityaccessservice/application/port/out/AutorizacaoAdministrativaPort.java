package br.com.escola.identityaccessservice.application.port.out;

import java.util.UUID;

public interface AutorizacaoAdministrativaPort {

    void autorizar(UUID usuarioId, String autoridade);
}
