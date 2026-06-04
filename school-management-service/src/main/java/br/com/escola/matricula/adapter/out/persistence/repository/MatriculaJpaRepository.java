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

    long countByStatus_CodigoIgnoreCase(String codigo);

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

    @Query("""
            select m.status.codigo as status, count(m.id) as total
              from MatriculaEntity m
             group by m.status.codigo
             order by m.status.codigo
            """)
    List<MatriculaStatusTotalProjection> countMatriculasPorStatus();

    boolean existsByAluno_IdAndPeriodoLetivo_Id(UUID alunoId, UUID periodoLetivoId);

    boolean existsByAluno_IdAndPeriodoLetivo_IdAndIdNot(
            UUID alunoId,
            UUID periodoLetivoId,
            UUID id);

    Optional<MatriculaEntity> findFirstByAluno_IdAndPeriodoLetivo_IdNotOrderByDataSolicitacaoDescCreatedAtDesc(
            UUID alunoId,
            UUID periodoLetivoId);
}
