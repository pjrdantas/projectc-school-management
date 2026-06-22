package br.com.escola.catalog.application.port.out;

import br.com.escola.catalog.application.event.BrokerPublication;
import br.com.escola.catalog.application.event.IntegrationEventEnvelope;

public interface EventBrokerPort {

    BrokerPublication publicar(String topic, String key, IntegrationEventEnvelope event);
}
