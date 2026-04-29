package br.com.escola.accesscontrol.adapter.out.persistence.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.accesscontrol.adapter.out.persistence.entity.SessaoAutenticacaoEntity;

public interface SessaoAutenticacaoJpaRepository extends JpaRepository<SessaoAutenticacaoEntity, UUID> {
    Optional<SessaoAutenticacaoEntity> findByRefreshTokenHashAndRevogadoFalseAndExpiraEmAfter(
            String refreshTokenHash,
            LocalDateTime instante
    );

    Optional<SessaoAutenticacaoEntity> findByAccessTokenHashAndRevogadoFalseAndAccessExpiraEmAfter(
            String accessTokenHash,
            LocalDateTime instante
    );
}
