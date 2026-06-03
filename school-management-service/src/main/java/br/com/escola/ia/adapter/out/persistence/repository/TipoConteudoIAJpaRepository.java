package br.com.escola.ia.adapter.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.ia.adapter.out.persistence.entity.TipoConteudoIAEntity;

public interface TipoConteudoIAJpaRepository extends JpaRepository<TipoConteudoIAEntity, UUID> {

    Optional<TipoConteudoIAEntity> findByCodigo(String codigo);
}
