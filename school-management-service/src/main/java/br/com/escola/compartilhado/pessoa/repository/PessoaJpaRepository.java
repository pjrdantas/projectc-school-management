package br.com.escola.compartilhado.pessoa.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.compartilhado.pessoa.entity.PessoaEntity;

public interface PessoaJpaRepository extends JpaRepository<PessoaEntity, UUID> {

    Optional<PessoaEntity> findByCpf(String cpf);

    Optional<PessoaEntity> findByCpfAndEscola_Id(String cpf, UUID escolaId);

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndEscola_Id(String cpf, UUID escolaId);
}
