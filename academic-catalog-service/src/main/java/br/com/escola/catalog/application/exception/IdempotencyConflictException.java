package br.com.escola.catalog.application.exception;

public class IdempotencyConflictException extends RuntimeException {

    public IdempotencyConflictException(String key) {
        super("Idempotency-Key reutilizada com outro comando ou payload: " + key);
    }
}
