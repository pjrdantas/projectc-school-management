package br.com.escola.catalog.infra.cache;

import java.util.Set;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import br.com.escola.catalog.application.event.IntegrationEventEnvelope;
import br.com.escola.catalog.application.port.out.CatalogReadCachePort;
import br.com.escola.catalog.domain.valueobject.EscolaId;

@Component
@ConditionalOnProperty(
        name = {"catalog.cache.enabled", "catalog.cache.kafka.enabled"},
        havingValue = "true")
public class CatalogCacheInvalidationListener {

    private static final Set<String> CATALOG_EVENTS = Set.of(
            "term-created", "grade-created", "subject-created",
            "class-created", "class-subject-created");

    private final CatalogReadCachePort cachePort;

    public CatalogCacheInvalidationListener(CatalogReadCachePort cachePort) {
        this.cachePort = cachePort;
    }

    @KafkaListener(
            topics = "${catalog.outbox.publisher.topic:school.catalog.events.v1}",
            groupId = "${catalog.cache.kafka.group-id:academic-catalog-cache-v1}",
            containerFactory = "catalogCacheKafkaListenerFactory")
    public void invalidar(IntegrationEventEnvelope event) {
        if (event.eventVersion() == 1 && CATALOG_EVENTS.contains(event.eventType())) {
            cachePort.invalidar(new EscolaId(event.escolaId()));
        }
    }
}
