package br.com.escola.catalog.application.port.out;

import java.time.Duration;
import java.util.Optional;

import br.com.escola.catalog.application.cache.CatalogReadSnapshot;
import br.com.escola.catalog.domain.valueobject.EscolaId;

public interface CatalogReadCachePort {

    Optional<CatalogReadSnapshot> buscar(EscolaId escolaId);

    void armazenar(EscolaId escolaId, CatalogReadSnapshot snapshot, Duration ttl);

    void invalidar(EscolaId escolaId);
}
