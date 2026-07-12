package br.com.escola.professor.adapter.out.persistence.repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.professor.adapter.out.persistence.entity.DiarioClasseLancamentoEntity;

public interface DiarioClasseLancamentoJpaRepository extends JpaRepository<DiarioClasseLancamentoEntity, UUID> {

    Optional<DiarioClasseLancamentoEntity>
            findByProfessorTurmaDisciplina_IdAndProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_IdAndDataLancamento(
                    UUID professorTurmaDisciplinaId,
                    UUID escolaId,
                    LocalDate dataLancamento);

    Optional<DiarioClasseLancamentoEntity> findByIdAndProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(
            UUID id,
            UUID escolaId);
}
