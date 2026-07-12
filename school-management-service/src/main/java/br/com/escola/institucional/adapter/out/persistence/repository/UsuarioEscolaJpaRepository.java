package br.com.escola.institucional.adapter.out.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.escola.institucional.adapter.out.persistence.entity.UsuarioEscolaEntity;

public interface UsuarioEscolaJpaRepository extends JpaRepository<UsuarioEscolaEntity, UUID> {

    boolean existsByUsuario_IdAndEscola_Id(UUID usuarioId, UUID escolaId);

    @Query("""
            SELECT ue.escola.id
            FROM UsuarioEscolaEntity ue
            WHERE ue.usuario.id = :usuarioId
            ORDER BY ue.createdAt ASC
            """)
    List<UUID> findEscolaIdsByUsuarioId(@Param("usuarioId") UUID usuarioId);
}
