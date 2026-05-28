package br.com.escola.academiccatalog.adapter.out.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.academiccatalog.adapter.out.persistence.entity.SerieEntity;

public interface SerieJpaRepository extends JpaRepository<SerieEntity, UUID> {
}
