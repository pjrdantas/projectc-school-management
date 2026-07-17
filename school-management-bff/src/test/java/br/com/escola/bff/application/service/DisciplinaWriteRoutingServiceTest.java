package br.com.escola.bff.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.DisciplinaCreateCommand;
import br.com.escola.bff.application.dto.DisciplinaCreatedResult;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.CatalogWriteObservabilityPort;
import br.com.escola.bff.application.port.out.CatalogoDisciplinaWritePort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class DisciplinaWriteRoutingServiceTest {

    @Test
    void deveUsarCatalogoComoOwnershipOficialMesmoComStatusInativa() {
        AtomicBoolean catalogCalled = new AtomicBoolean(false);
        CatalogoDisciplinaWritePort catalog = (query, context, command) -> {
            catalogCalled.set(true);
            return Mono.just(resultado("INATIVA", "catalog"));
        };
        AuthContextPort authContext = query -> Mono.just(new AuthSessionContext(UUID.randomUUID(), UUID.randomUUID()));
        CatalogWriteObservabilityPort observability = new NoOpObservability();

        DisciplinaWriteRoutingService service = new DisciplinaWriteRoutingService(catalog, authContext, observability);

        StepVerifier.create(service.executar(query(), command("INATIVA", null)))
                .assertNext(response -> assertThat(response.status()).isEqualTo("INATIVA"))
                .verifyComplete();

        assertThat(catalogCalled.get()).isTrue();
    }

    @Test
    void naoDeveFazerFallbackParaMonolitoQuandoCatalogoFalhar() {
        CatalogoDisciplinaWritePort catalog = (query, context, command) ->
                Mono.error(new DownstreamUnavailableException("catalog indisponivel"));
        AuthContextPort authContext = query -> Mono.just(new AuthSessionContext(UUID.randomUUID(), UUID.randomUUID(), "Escola A"));
        CatalogWriteObservabilityPort observability = new NoOpObservability();

        DisciplinaWriteRoutingService service = new DisciplinaWriteRoutingService(catalog, authContext, observability);

        StepVerifier.create(service.executar(query(), command("ATIVA", null)))
                .expectError(DownstreamUnavailableException.class)
                .verify();
    }

    @Test
    void deveRejeitarEscolaIdDiferenteDoContextoAutenticado() {
        UUID escolaContexto = UUID.randomUUID();
        AuthContextPort authContext = query -> Mono.just(new AuthSessionContext(UUID.randomUUID(), escolaContexto, "Escola A"));
        CatalogoDisciplinaWritePort catalog = (query, context, command) -> Mono.just(resultado("ATIVA", "catalog"));
        CatalogWriteObservabilityPort observability = new NoOpObservability();

        DisciplinaWriteRoutingService service = new DisciplinaWriteRoutingService(catalog, authContext, observability);

        StepVerifier.create(service.executar(query(), command("ATIVA", UUID.randomUUID())))
                .expectErrorMatches(error -> error instanceof IllegalArgumentException
                        && error.getMessage().contains("escolaId informado diverge"))
                .verify();
    }

    private static CatalogWriteQuery query() {
        return new CatalogWriteQuery("Bearer token", "corr-1", "idem-1");
    }

    private static DisciplinaCreateCommand command(String status, UUID escolaId) {
        return new DisciplinaCreateCommand("Matematica", 80, status, escolaId);
    }

    private static DisciplinaCreatedResult resultado(String status, String escolaNome) {
        return new DisciplinaCreatedResult(
                UUID.randomUUID(),
                "Matematica",
                80,
                status,
                UUID.randomUUID(),
                escolaNome,
                LocalDateTime.now());
    }

    private static final class NoOpObservability implements CatalogWriteObservabilityPort {
        @Override public void recordCatalogSuccess(CatalogWriteCutoverDecision decision) {}
        @Override public void recordCatalogFailure(CatalogWriteCutoverDecision decision, Throwable error) {}
    }
}
