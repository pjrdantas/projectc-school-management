package br.com.escola.compartilhado.endereco.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.compartilhado.endereco.entity.TipoEnderecoEntity;

public interface TipoEnderecoJpaRepository extends JpaRepository<TipoEnderecoEntity, UUID> {

    Optional<TipoEnderecoEntity> findByCodigo(String codigo);
}
