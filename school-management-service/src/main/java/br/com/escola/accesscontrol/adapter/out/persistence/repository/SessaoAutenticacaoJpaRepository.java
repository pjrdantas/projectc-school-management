package br.com.escola.accesscontrol.adapter.out.persistence.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.escola.accesscontrol.adapter.out.persistence.entity.SessaoAutenticacaoEntity;

public interface SessaoAutenticacaoJpaRepository extends JpaRepository<SessaoAutenticacaoEntity, UUID> {

    @Query("""
            SELECT s
            FROM SessaoAutenticacaoEntity s
            JOIN FETCH s.usuario u
            WHERE s.refreshTokenHash = :refreshTokenHash
              AND s.revogado = false
              AND s.expiraEm > :instante
            """)
    Optional<SessaoAutenticacaoEntity> findByRefreshTokenHashAndRevogadoFalseAndExpiraEmAfter(
            @Param("refreshTokenHash") String refreshTokenHash,
            @Param("instante") LocalDateTime instante
    );

    @Query("""
            SELECT s
            FROM SessaoAutenticacaoEntity s
            JOIN FETCH s.usuario u
            WHERE s.accessTokenHash = :accessTokenHash
              AND s.revogado = false
              AND s.accessExpiraEm > :instante
            """)
    Optional<SessaoAutenticacaoEntity> findByAccessTokenHashAndRevogadoFalseAndAccessExpiraEmAfter(
            @Param("accessTokenHash") String accessTokenHash,
            @Param("instante") LocalDateTime instante
    );
}
