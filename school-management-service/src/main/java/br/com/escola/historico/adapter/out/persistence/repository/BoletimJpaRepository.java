package br.com.escola.historico.adapter.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.com.escola.historico.adapter.out.persistence.entity.BoletimEntity;

public interface BoletimJpaRepository extends JpaRepository<BoletimEntity, UUID> {

    List<BoletimEntity> findByMatriculaId(UUID matriculaId);

    List<BoletimEntity> findByMatricula_IdAndMatricula_Turma_Escola_Id(UUID matriculaId, UUID escolaId);

    Optional<BoletimEntity> findByMatriculaIdAndPeriodoReferencia(UUID matriculaId, String periodoReferencia);

    Optional<BoletimEntity> findByMatricula_IdAndMatricula_Turma_Escola_IdAndPeriodoReferencia(
            UUID matriculaId,
            UUID escolaId,
            String periodoReferencia);

    Optional<BoletimEntity> findByIdAndMatricula_Turma_Escola_Id(UUID id, UUID escolaId);

    @Query(value = """
            select count(distinct b.id_boletim)
              from boletim b
             where exists (
                   select 1
                     from boletim_item bi
                    where bi.id_boletim = b.id_boletim
             )
               and not exists (
                   select 1
                     from boletim_item bi
                    where bi.id_boletim = b.id_boletim
                      and upper(coalesce(bi.resultado, '')) in ('REPROVADO', 'PENDENTE')
             )
            """, nativeQuery = true)
    long countBoletinsAprovados();

    @Query(value = """
            select count(distinct b.id_boletim)
              from boletim b
              join boletim_item bi on bi.id_boletim = b.id_boletim
             where upper(coalesce(bi.resultado, '')) = 'REPROVADO'
            """, nativeQuery = true)
    long countBoletinsReprovados();
}
