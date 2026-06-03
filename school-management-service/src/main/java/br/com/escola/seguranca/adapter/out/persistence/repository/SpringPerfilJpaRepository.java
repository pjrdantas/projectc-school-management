package br.com.escola.seguranca.adapter.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.escola.seguranca.adapter.out.persistence.entity.PerfilEntity;

@Repository
public interface SpringPerfilJpaRepository extends JpaRepository<PerfilEntity, UUID> {

    boolean existsByCodigo(String codigo);

    Optional<PerfilEntity> findByCodigo(String codigo);

    @Modifying
    @Query(value = """
            INSERT INTO perfil (id_perfil, codigo, nome, descricao, created_at)
            VALUES (:id, :codigo, :nome, :descricao, CURRENT_TIMESTAMP)
            """, nativeQuery = true)
    void insertPerfil(@Param("id") UUID id,
                      @Param("codigo") String codigo,
                      @Param("nome") String nome,
                      @Param("descricao") String descricao);

    @Modifying
    @Query(value = """
            UPDATE perfil
               SET codigo = :codigo,
                   nome = :nome,
                   descricao = :descricao
             WHERE id_perfil = :id
            """, nativeQuery = true)
    void updatePerfilFields(@Param("id") UUID id,
                            @Param("codigo") String codigo,
                            @Param("nome") String nome,
                            @Param("descricao") String descricao);

    @Modifying
    @Query(value = "DELETE FROM perfil_permissao WHERE id_perfil = :perfilId", nativeQuery = true)
    void deletePermissoesByPerfilId(@Param("perfilId") UUID perfilId);

    @Modifying
    @Query(value = """
            INSERT INTO perfil_permissao (id_perfil_permissao, id_perfil, id_permissao, created_at)
            VALUES (:idPerfilPermissao, :perfilId, :permissaoId, CURRENT_TIMESTAMP)
            """, nativeQuery = true)
    void insertPerfilPermissao(@Param("idPerfilPermissao") UUID idPerfilPermissao,
                               @Param("perfilId") UUID perfilId,
                               @Param("permissaoId") UUID permissaoId);

}