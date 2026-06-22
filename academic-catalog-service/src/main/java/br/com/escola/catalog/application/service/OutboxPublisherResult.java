package br.com.escola.catalog.application.service;

public record OutboxPublisherResult(int claimed, int published, int retry, int dlt) {
}
