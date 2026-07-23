package br.com.escola.catalog.application.port.out;

import java.time.Duration;
import java.util.Optional;

import br.com.escola.catalog.application.cache.LeituraSnapshot;
import br.com.escola.catalog.domain.valueobject.EscolaId;

public interface LeituraCachePort {

    Optional<LeituraSnapshot> buscar(EscolaId escolaId);

    void armazenar(EscolaId escolaId, LeituraSnapshot snapshot, Duration ttl);

    void invalidar(EscolaId escolaId);
}

