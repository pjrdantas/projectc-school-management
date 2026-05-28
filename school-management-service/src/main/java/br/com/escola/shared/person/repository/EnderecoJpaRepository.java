package br.com.escola.shared.person.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.shared.person.entity.EnderecoEntity;

public interface EnderecoJpaRepository extends JpaRepository<EnderecoEntity, UUID> {
}
