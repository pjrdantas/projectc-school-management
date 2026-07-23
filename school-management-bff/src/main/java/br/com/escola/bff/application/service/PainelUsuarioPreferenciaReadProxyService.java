package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.PainelUsuarioPreferenciaReadPort;
import br.com.escola.bff.application.usecase.ListarPainelUsuarioPreferenciaUseCase;
import reactor.core.publisher.Mono;

public class PainelUsuarioPreferenciaReadProxyService implements ListarPainelUsuarioPreferenciaUseCase {

    private final InternalAuthContextPort authContextPort;
    private final PainelUsuarioPreferenciaReadPort preferenciaReadPort;

    public PainelUsuarioPreferenciaReadProxyService(
            InternalAuthContextPort authContextPort, PainelUsuarioPreferenciaReadPort preferenciaReadPort) {
        this.authContextPort = authContextPort;
        this.preferenciaReadPort = preferenciaReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> listar(String authorization, String correlationId, UUID usuarioId, UUID painelId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> preferenciaReadPort.listar(usuarioId, painelId, query, context));
    }
}
