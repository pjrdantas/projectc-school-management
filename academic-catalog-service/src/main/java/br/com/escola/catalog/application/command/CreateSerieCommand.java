package br.com.escola.catalog.application.command;

import java.util.UUID;

public record CreateSerieCommand(String nome, int ordem, UUID nivelEnsinoId) {
}
