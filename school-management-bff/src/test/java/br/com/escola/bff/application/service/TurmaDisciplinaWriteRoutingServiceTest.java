package br.com.escola.bff.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogWriteQuery;
import br.com.escola.bff.application.dto.TurmaDisciplinaLinkCommand;
import br.com.escola.bff.application.dto.TurmaDisciplinaLinkedResult;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.CatalogoTurmaDisciplinaWritePort;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.CatalogWriteObservabilityPort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class TurmaDisciplinaWriteRoutingServiceTest {

    @Test
    void deveUsarCatalogoComoDestinoOficial() {
        CatalogoTurmaDisciplinaWritePort catalog = (query, context, command) -> Mono.just(resultado("catalog"));
        AuthContextPort authContext = query -> Mono.just(new AuthSessionContext(UUID.randomUUID(), UUID.randomUUID()));
        CatalogWriteObservabilityPort observability = new NoOpObservability();

        TurmaDisciplinaWriteRoutingService service = new TurmaDisciplinaWriteRoutingService(
                catalog, authContext, observability);

        StepVerifier.create(service.executar(query(), command()))
                .assertNext(response -> assertThat(response.disciplinaNome()).isEqualTo("catalog"))
                .verifyComplete();
    }

    @Test
    void naoDeveFazerFallbackParaMonolitoQuandoCatalogoFalhar() {
        CatalogoTurmaDisciplinaWritePort catalog = (query, context, command) ->
                Mono.error(new DownstreamUnavailableException("catalog indisponivel"));
        AuthContextPort authContext = query -> Mono.just(new AuthSessionContext(UUID.randomUUID(), UUID.randomUUID()));
        CatalogWriteObservabilityPort observability = new NoOpObservability();

        TurmaDisciplinaWriteRoutingService service = new TurmaDisciplinaWriteRoutingService(
                catalog, authContext, observability);

        StepVerifier.create(service.executar(query(), command()))
                .expectError(DownstreamUnavailableException.class)
                .verify();
    }

    private static CatalogWriteQuery query() {
        return new CatalogWriteQuery("Bearer token", "corr-1", "idem-1");
    }

    private static TurmaDisciplinaLinkCommand command() {
        return new TurmaDisciplinaLinkCommand(UUID.randomUUID(), UUID.randomUUID(), 80);
    }

    private static TurmaDisciplinaLinkedResult resultado(String disciplinaNome) {
        return new TurmaDisciplinaLinkedResult(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                disciplinaNome,
                80,
                LocalDateTime.now());
    }

    private static final class NoOpObservability implements CatalogWriteObservabilityPort {
        @Override public void recordCatalogSuccess(CatalogWriteCutoverDecision decision) {}
        @Override public void recordCatalogFailure(CatalogWriteCutoverDecision decision, Throwable error) {}
    }
}

