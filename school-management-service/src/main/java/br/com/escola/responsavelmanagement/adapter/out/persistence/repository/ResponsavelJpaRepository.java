package br.com.escola.responsavelmanagement.adapter.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.responsavelmanagement.adapter.out.persistence.entity.ResponsavelEntity;

public interface ResponsavelJpaRepository extends JpaRepository<ResponsavelEntity, UUID> {

    Optional<ResponsavelEntity> findByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, UUID id);
}
