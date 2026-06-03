package br.com.escola.aluno.adapter.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.aluno.adapter.out.persistence.entity.TipoEventoAlunoEntity;

public interface TipoEventoAlunoJpaRepository extends JpaRepository<TipoEventoAlunoEntity, UUID> {

    Optional<TipoEventoAlunoEntity> findByCodigo(String codigo);
}
