package br.com.escola.catalog.infra.cache;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import br.com.escola.catalog.application.event.IntegrationEventEnvelope;
import br.com.escola.catalog.application.port.out.CatalogReadCachePort;
import br.com.escola.catalog.domain.valueobject.EscolaId;

class CatalogCacheInvalidationListenerTest {

    @Test
    void deveInvalidarSomenteEventoVersionadoDoCatalogo() {
        CatalogReadCachePort cache = Mockito.mock(CatalogReadCachePort.class);
        CatalogCacheInvalidationListener listener = new CatalogCacheInvalidationListener(cache);
        UUID escolaId = UUID.randomUUID();

        listener.invalidar(event("subject-created", 1, escolaId));
        listener.invalidar(event("student-changed", 1, escolaId));

        verify(cache).invalidar(new EscolaId(escolaId));
        verify(cache, never()).armazenar(Mockito.any(), Mockito.any(), Mockito.any());
    }

    private IntegrationEventEnvelope event(String type, int version, UUID escolaId) {
        return new IntegrationEventEnvelope(
                UUID.randomUUID(), type, version, Instant.now(), "corr-cache", null,
                UUID.randomUUID(), escolaId, Map.of());
    }
}
