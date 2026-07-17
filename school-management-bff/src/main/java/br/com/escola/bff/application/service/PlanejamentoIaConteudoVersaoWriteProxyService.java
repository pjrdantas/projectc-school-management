package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.PlanejamentoIaConteudoVersaoWritePort;
import br.com.escola.bff.application.usecase.CriarPlanejamentoIaConteudoVersaoUseCase;
import reactor.core.publisher.Mono;

public class PlanejamentoIaConteudoVersaoWriteProxyService implements CriarPlanejamentoIaConteudoVersaoUseCase {

    private final AuthContextPort authContextPort;
    private final PlanejamentoIaConteudoVersaoWritePort planningAiConteudoVersaoWritePort;

    public PlanejamentoIaConteudoVersaoWriteProxyService(
            AuthContextPort authContextPort,
            PlanejamentoIaConteudoVersaoWritePort planningAiConteudoVersaoWritePort) {
        this.authContextPort = authContextPort;
        this.planningAiConteudoVersaoWritePort = planningAiConteudoVersaoWritePort;
    }

    @Override
    public Mono<ResponseEntity<String>> executar(
            String authorization,
            String correlationId,
            UUID conteudoId,
            String requestBody) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> planningAiConteudoVersaoWritePort.criarVersao(
                        conteudoId,
                        requestBody,
                        query,
                        context));
    }
}

