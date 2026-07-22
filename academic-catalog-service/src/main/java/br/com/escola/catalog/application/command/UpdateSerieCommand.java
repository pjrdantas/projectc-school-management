package br.com.escola.catalog.application.command;

import java.util.UUID;

public record UpdateSerieCommand(String nome, int ordem, UUID nivelEnsinoId) {
}
