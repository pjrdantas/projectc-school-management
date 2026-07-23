package br.com.escola.professorservice.infra.database.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.professorservice.infra.database.entity.CadastroJpaEntity;

public interface CadastroJpaRepository extends JpaRepository<CadastroJpaEntity, UUID> {

    List<CadastroJpaEntity> findAllByEscolaIdOrderByNomeCompletoAscIdAsc(UUID escolaId);

    Optional<CadastroJpaEntity> findByPessoaIdAndEscolaId(UUID pessoaId, UUID escolaId);
}

