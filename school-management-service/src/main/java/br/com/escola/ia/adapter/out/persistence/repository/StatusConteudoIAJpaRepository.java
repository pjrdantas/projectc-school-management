package br.com.escola.ia.adapter.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.ia.adapter.out.persistence.entity.StatusConteudoIAEntity;

public interface StatusConteudoIAJpaRepository extends JpaRepository<StatusConteudoIAEntity, UUID> {

    Optional<StatusConteudoIAEntity> findByCodigo(String codigo);
}
