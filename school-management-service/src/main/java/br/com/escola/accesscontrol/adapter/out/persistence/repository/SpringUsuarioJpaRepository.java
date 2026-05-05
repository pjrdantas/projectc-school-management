package br.com.escola.accesscontrol.adapter.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.escola.accesscontrol.adapter.out.persistence.entity.UsuarioEntity;

public interface SpringUsuarioJpaRepository extends JpaRepository<UsuarioEntity, UUID> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<UsuarioEntity> findByUsernameIgnoreCaseAndAtivoTrue(String username);
    Optional<UsuarioEntity> findByUsernameIgnoreCase(String username);
    Optional<UsuarioEntity> findByEmailIgnoreCaseAndAtivoTrue(String email);

    @Query(value = """
            SELECT DISTINCT p.codigo
            FROM permissao p
            JOIN perfil_permissao pp ON pp.id_permissao = p.id_permissao
            JOIN usuario_perfil up ON up.id_perfil = pp.id_perfil
            WHERE up.id_usuario = :idUsuario
            """, nativeQuery = true)
    List<String> findPermissoesByIdUsuario(@Param("idUsuario") UUID idUsuario);

    @Query(value = """
            SELECT DISTINCT pf.codigo
            FROM perfil pf
            JOIN usuario_perfil up ON up.id_perfil = pf.id_perfil
            WHERE up.id_usuario = :idUsuario
            """, nativeQuery = true)
    List<String> findPerfisByIdUsuario(@Param("idUsuario") UUID idUsuario);
    
    boolean existsByUsernameAndIdNot(String username, UUID id);
}
