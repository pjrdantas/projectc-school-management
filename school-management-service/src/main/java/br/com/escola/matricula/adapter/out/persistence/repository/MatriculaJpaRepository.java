package br.com.escola.matricula.adapter.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import br.com.escola.matricula.adapter.out.persistence.entity.MatriculaEntity;
public interface MatriculaJpaRepository extends JpaRepository<MatriculaEntity, UUID>, JpaSpecificationExecutor<MatriculaEntity> {

    long countByTurma_IdAndStatus_CodigoNotIn(UUID turmaId, Iterable<String> statusCodigos);

    long countByTurma_IdAndTurma_Escola_IdAndStatus_CodigoNotIn(
            UUID turmaId,
            UUID escolaId,
            Iterable<String> statusCodigos);

    long countByStatus_CodigoIgnoreCase(String codigo);

    long countByTurma_Escola_Id(UUID escolaId);

    long countByTurma_Escola_IdAndStatus_CodigoIgnoreCase(UUID escolaId, String codigo);

    @Query(value = """
            select count(distinct m.id_matricula)
              from matricula m
              join matricula_documento_exigido exigido
                on exigido.id_tipo_matricula = m.id_tipo_matricula
               and exigido.obrigatorio = true
             where not exists (
                   select 1
                     from matricula_documento_entregue entregue
                     join documento d on d.id_documento = entregue.id_documento
                    where entregue.id_matricula = m.id_matricula
                      and d.id_tipo_documento = exigido.id_tipo_documento
                      and entregue.conferido = true
             )
            """, nativeQuery = true)
    long countMatriculasComDocumentosObrigatoriosPendentes();

    @Query(value = """
            select count(distinct m.id_matricula)
              from matricula m
              join turma t on t.id_turma = m.id_turma
              join matricula_documento_exigido exigido
                on exigido.id_tipo_matricula = m.id_tipo_matricula
               and exigido.obrigatorio = true
             where t.id_escola = :escolaId
               and not exists (
                   select 1
                     from matricula_documento_entregue entregue
                     join documento d on d.id_documento = entregue.id_documento
                    where entregue.id_matricula = m.id_matricula
                      and d.id_tipo_documento = exigido.id_tipo_documento
                      and entregue.conferido = true
             )
            """, nativeQuery = true)
    long countMatriculasComDocumentosObrigatoriosPendentesByEscolaId(UUID escolaId);

    @Query("""
            select m.status.codigo as status, count(m.id) as total
              from MatriculaEntity m
             group by m.status.codigo
             order by m.status.codigo
            """)
    List<MatriculaStatusTotalProjection> countMatriculasPorStatus();

    @Query("""
            select m.status.codigo as status, count(m.id) as total
              from MatriculaEntity m
             where m.turma.escola.id = :escolaId
             group by m.status.codigo
             order by m.status.codigo
            """)
    List<MatriculaStatusTotalProjection> countMatriculasPorStatusByEscolaId(UUID escolaId);

    boolean existsByAluno_IdAndPeriodoLetivo_Id(UUID alunoId, UUID periodoLetivoId);

    boolean existsByAluno_IdAndAluno_Pessoa_Escola_IdAndPeriodoLetivo_Id(
            UUID alunoId,
            UUID escolaId,
            UUID periodoLetivoId);

    boolean existsByAluno_IdAndPeriodoLetivo_IdAndIdNot(
            UUID alunoId,
            UUID periodoLetivoId,
            UUID id);

    boolean existsByAluno_IdAndAluno_Pessoa_Escola_IdAndPeriodoLetivo_IdAndIdNot(
            UUID alunoId,
            UUID escolaId,
            UUID periodoLetivoId,
            UUID id);

    boolean existsByIdAndTurma_Escola_Id(UUID id, UUID escolaId);

    Optional<MatriculaEntity> findByIdAndTurma_Escola_Id(UUID id, UUID escolaId);

    Optional<MatriculaEntity> findFirstByAluno_IdAndPeriodoLetivo_IdNotOrderByDataSolicitacaoDescCreatedAtDesc(
            UUID alunoId,
            UUID periodoLetivoId);

    Optional<MatriculaEntity> findFirstByAluno_IdAndAluno_Pessoa_Escola_IdAndPeriodoLetivo_IdNotOrderByDataSolicitacaoDescCreatedAtDesc(
            UUID alunoId,
            UUID escolaId,
            UUID periodoLetivoId);
}
