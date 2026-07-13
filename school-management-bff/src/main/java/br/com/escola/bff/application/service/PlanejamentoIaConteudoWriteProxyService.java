package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.PlanningAiConteudoWritePort;
import br.com.escola.bff.application.usecase.CriarPlanejamentoIaConteudoUseCase;
import reactor.core.publisher.Mono;

public class PlanejamentoIaConteudoWriteProxyService implements CriarPlanejamentoIaConteudoUseCase {

    private final AuthContextPort authContextPort;
    private final PlanningAiConteudoWritePort planningAiConteudoWritePort;

    public PlanejamentoIaConteudoWriteProxyService(
            AuthContextPort authContextPort,
            PlanningAiConteudoWritePort planningAiConteudoWritePort) {
        this.authContextPort = authContextPort;
        this.planningAiConteudoWritePort = planningAiConteudoWritePort;
    }

    @Override
    public Mono<ResponseEntity<String>> executar(
            String authorization,
            String correlationId,
            UUID planejamentoId,
            String requestBody) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> planningAiConteudoWritePort.criarConteudo(
                        planejamentoId,
                        requestBody,
                        query,
                        context));
    }
}
