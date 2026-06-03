package br.com.escola.compartilhado.endereco.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.compartilhado.endereco.entity.EnderecoEntity;

public interface EnderecoJpaRepository extends JpaRepository<EnderecoEntity, UUID> {
}
