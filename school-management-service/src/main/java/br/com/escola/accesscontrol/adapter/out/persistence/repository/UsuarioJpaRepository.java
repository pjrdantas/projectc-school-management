package br.com.escola.accesscontrol.adapter.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.com.escola.accesscontrol.adapter.out.persistence.entity.UsuarioEntity;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, UUID> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<UsuarioEntity> findByUsernameIgnoreCaseAndAtivoTrue(String username);
    Optional<UsuarioEntity> findByEmailIgnoreCaseAndAtivoTrue(String email);

    @Query(value = """
            SELECT DISTINCT p.codigo
            FROM permissao p
            JOIN perfil_permissao pp ON pp.id_permissao = p.id_permissao
            JOIN usuario_perfil up ON up.id_perfil = pp.id_perfil
            WHERE up.id_usuario = :idUsuario
            """, nativeQuery = true)
    List<String> findPermissoesByIdUsuario(UUID idUsuario);
}
