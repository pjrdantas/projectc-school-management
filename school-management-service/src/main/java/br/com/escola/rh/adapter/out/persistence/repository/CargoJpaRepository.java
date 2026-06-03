package br.com.escola.rh.adapter.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.rh.adapter.out.persistence.entity.CargoEntity;

public interface CargoJpaRepository extends JpaRepository<CargoEntity, UUID> {

    Optional<CargoEntity> findByCodigo(String codigo);
}
