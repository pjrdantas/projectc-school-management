package br.com.escola.responsavel.adapter.out.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import br.com.escola.responsavel.adapter.out.persistence.entity.AlunoResponsavelEntity;

public interface AlunoResponsavelJpaRepository extends JpaRepository<AlunoResponsavelEntity, UUID> {

    boolean existsByIdAlunoAndIdResponsavel(UUID idAluno, UUID idResponsavel);

    @Query(value = """
            SELECT COUNT(1) > 0
              FROM aluno_responsavel ar
              JOIN aluno a ON a.id_aluno = ar.id_aluno
              JOIN pessoa pa ON pa.id_pessoa = a.id_pessoa
              JOIN responsavel r ON r.id_responsavel = ar.id_responsavel
              JOIN pessoa pr ON pr.id_pessoa = r.id_pessoa
             WHERE ar.id_aluno = :idAluno
               AND ar.id_responsavel = :idResponsavel
               AND pa.id_escola = :escolaId
               AND pr.id_escola = :escolaId
            """, nativeQuery = true)
    boolean existsByAlunoAndResponsavelAndEscolaId(UUID idAluno, UUID idResponsavel, UUID escolaId);

    List<AlunoResponsavelEntity> findByIdAluno(UUID idAluno);

    @Query(value = """
            SELECT ar.*
              FROM aluno_responsavel ar
              JOIN aluno a ON a.id_aluno = ar.id_aluno
              JOIN pessoa pa ON pa.id_pessoa = a.id_pessoa
              JOIN responsavel r ON r.id_responsavel = ar.id_responsavel
              JOIN pessoa pr ON pr.id_pessoa = r.id_pessoa
             WHERE ar.id_aluno = :idAluno
               AND pa.id_escola = :escolaId
               AND pr.id_escola = :escolaId
             ORDER BY ar.created_at
            """, nativeQuery = true)
    List<AlunoResponsavelEntity> findByAlunoAndEscolaId(UUID idAluno, UUID escolaId);

    long countByIdResponsavel(UUID idResponsavel);

    void deleteByIdAluno(UUID idAluno);

    void deleteByIdAlunoAndIdResponsavel(UUID idAluno, UUID idResponsavel);

    @Modifying
    @Query(value = """
            DELETE FROM aluno_responsavel
             WHERE id_aluno = :idAluno
               AND id_responsavel = :idResponsavel
               AND EXISTS (
                   SELECT 1
                     FROM aluno a
                     JOIN pessoa pa ON pa.id_pessoa = a.id_pessoa
                     JOIN responsavel r ON r.id_responsavel = aluno_responsavel.id_responsavel
                     JOIN pessoa pr ON pr.id_pessoa = r.id_pessoa
                    WHERE a.id_aluno = aluno_responsavel.id_aluno
                      AND pa.id_escola = :escolaId
                      AND pr.id_escola = :escolaId
               )
            """, nativeQuery = true)
    void deleteByAlunoAndResponsavelAndEscolaId(UUID idAluno, UUID idResponsavel, UUID escolaId);
}
