package br.com.escola.catalog.application.event;

public record BrokerPublication(String topic, int partition, long offset) {
}
