package br.com.escola.catalog.application.command;

public record CommandResult<T>(T response, boolean replayed) {
}
