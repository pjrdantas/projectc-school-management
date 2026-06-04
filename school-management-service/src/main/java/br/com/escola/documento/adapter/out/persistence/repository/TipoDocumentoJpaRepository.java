package br.com.escola.documento.adapter.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.documento.adapter.out.persistence.entity.TipoDocumentoEntity;

public interface TipoDocumentoJpaRepository extends JpaRepository<TipoDocumentoEntity, UUID> {

    Optional<TipoDocumentoEntity> findByCodigoIgnoreCase(String codigo);
}
