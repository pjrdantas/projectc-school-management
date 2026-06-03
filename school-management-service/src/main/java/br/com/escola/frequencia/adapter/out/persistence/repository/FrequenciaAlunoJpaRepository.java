package br.com.escola.frequencia.adapter.out.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.frequencia.adapter.out.persistence.entity.FrequenciaAlunoEntity;

public interface FrequenciaAlunoJpaRepository extends JpaRepository<FrequenciaAlunoEntity, UUID> {

    List<FrequenciaAlunoEntity> findByMatriculaId(UUID matriculaId);
}
