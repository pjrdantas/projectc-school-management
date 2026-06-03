package br.com.escola.avaliacao.adapter.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.avaliacao.adapter.out.persistence.entity.TipoAvaliacaoEntity;

public interface TipoAvaliacaoJpaRepository extends JpaRepository<TipoAvaliacaoEntity, UUID> {

    Optional<TipoAvaliacaoEntity> findByCodigo(String codigo);
}
