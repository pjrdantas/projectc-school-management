package br.com.escola.catalog.application.port.out;

import java.time.Duration;

public interface IdempotencyPort {

    boolean registrarSeAusente(String key, Duration ttl);
}

