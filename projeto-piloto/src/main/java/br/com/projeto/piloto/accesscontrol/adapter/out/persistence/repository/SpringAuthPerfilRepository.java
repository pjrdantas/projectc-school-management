package br.com.projeto.piloto.accesscontrol.adapter.out.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.projeto.piloto.accesscontrol.adapter.out.persistence.entity.AuthPerfil;

@Repository
public interface SpringAuthPerfilRepository extends JpaRepository<AuthPerfil, Long> {

   
    boolean existsByNmPerfil(String nmPerfil);

    Optional<AuthPerfil> findByNmPerfil(String nmPerfil);
}
