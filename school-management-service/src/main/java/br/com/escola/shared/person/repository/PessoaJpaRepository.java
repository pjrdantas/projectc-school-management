package br.com.escola.shared.person.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.shared.person.entity.PessoaEntity;

public interface PessoaJpaRepository extends JpaRepository<PessoaEntity, UUID> {

    Optional<PessoaEntity> findByCpf(String cpf);

    boolean existsByCpf(String cpf);
}
