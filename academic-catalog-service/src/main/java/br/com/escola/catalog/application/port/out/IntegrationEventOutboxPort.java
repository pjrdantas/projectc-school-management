package br.com.escola.catalog.application.port.out;

import br.com.escola.catalog.application.event.OutboxEvent;

public interface IntegrationEventOutboxPort {

    void adicionar(OutboxEvent event);
}
