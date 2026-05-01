package br.com.projeto.piloto.adapter.out.jpa.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.com.projeto.piloto.adapter.out.jpa.entity.AuthSessao;

@Repository
public interface AuthSessaoRepository extends JpaRepository<AuthSessao, Long> {


    Optional<AuthSessao> findByTokenAndAtivoAndDataExpiracaoAfter(String token, String ativo, java.time.LocalDateTime dataExpiracao);

    List<AuthSessao> findByAuthUsuarioIdAndAtivo(Long authUsuarioId, String ativo);

    Optional<AuthSessao> findByRefreshToken(String refreshToken);

    @Modifying
    @Query("UPDATE AuthSessao s SET s.ativo = 'N' WHERE s.authUsuarioId = :authUsuarioId AND s.ativo = 'S'")
    void invalidarTodasSessoes(@Param("authUsuarioId") Long authUsuarioId);

    @Modifying
    @Query("UPDATE AuthSessao s SET s.ativo = 'N' WHERE s.id = :id")
    void invalidarSessao(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("DELETE FROM AuthSessao s WHERE s.authUsuarioId = :authUsuarioId AND s.ativo = 'N'")
    void deletarSessoesInativas(@Param("authUsuarioId") Long authUsuarioId);

    @Modifying
    @Transactional
    void deleteByDataExpiracaoBefore(java.time.LocalDateTime agora);
}
