package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.PlanejamentoIaConteudoBibliotecaWritePort;
import br.com.escola.bff.application.usecase.PublicarPlanejamentoIaConteudoBibliotecaUseCase;
import reactor.core.publisher.Mono;

public class PlanejamentoIaConteudoBibliotecaWriteProxyService implements PublicarPlanejamentoIaConteudoBibliotecaUseCase {

    private final AuthContextPort authContextPort;
    private final PlanejamentoIaConteudoBibliotecaWritePort planningAiConteudoBibliotecaWritePort;

    public PlanejamentoIaConteudoBibliotecaWriteProxyService(
            AuthContextPort authContextPort,
            PlanejamentoIaConteudoBibliotecaWritePort planningAiConteudoBibliotecaWritePort) {
        this.authContextPort = authContextPort;
        this.planningAiConteudoBibliotecaWritePort = planningAiConteudoBibliotecaWritePort;
    }

    @Override
    public Mono<ResponseEntity<String>> executar(
            String authorization,
            String correlationId,
            UUID conteudoId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> planningAiConteudoBibliotecaWritePort.publicarBiblioteca(
                        conteudoId,
                        query,
                        context));
    }
}

