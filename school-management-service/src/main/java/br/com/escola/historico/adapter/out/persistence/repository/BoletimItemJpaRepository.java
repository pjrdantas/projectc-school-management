package br.com.escola.historico.adapter.out.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.historico.adapter.out.persistence.entity.BoletimItemEntity;

public interface BoletimItemJpaRepository extends JpaRepository<BoletimItemEntity, UUID> {

    List<BoletimItemEntity> findByBoletimId(UUID boletimId);
}
