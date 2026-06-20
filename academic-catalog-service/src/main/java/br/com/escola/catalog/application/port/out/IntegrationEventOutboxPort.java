package br.com.escola.catalog.application.port.out;

import br.com.escola.catalog.application.event.IntegrationEventEnvelope;

public interface IntegrationEventOutboxPort {

    void adicionar(IntegrationEventEnvelope event);
}

