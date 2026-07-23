package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.PlanejamentoIaInteracaoReadPort;
import br.com.escola.bff.application.usecase.ConsultarPlanejamentoIaInteracaoUseCase;
import reactor.core.publisher.Mono;

public class PlanejamentoIaInteracaoReadProxyService implements ConsultarPlanejamentoIaInteracaoUseCase {

    private final InternalAuthContextPort authContextPort;
    private final PlanejamentoIaInteracaoReadPort planningAiInteracaoReadPort;

    public PlanejamentoIaInteracaoReadProxyService(
            InternalAuthContextPort authContextPort,
            PlanejamentoIaInteracaoReadPort planningAiInteracaoReadPort) {
        this.authContextPort = authContextPort;
        this.planningAiInteracaoReadPort = planningAiInteracaoReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> listarInteracoes(
            String authorization,
            String correlationId,
            UUID planejamentoId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> planningAiInteracaoReadPort.listarInteracoes(
                        planejamentoId,
                        query,
                        context));
    }
}

