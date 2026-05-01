package br.com.projeto.piloto.adapter.out.jpa.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.projeto.piloto.adapter.out.jpa.entity.AuthUsuario;

public interface SpringAuthUsuarioRepository extends JpaRepository<AuthUsuario, Long> {

    Optional<AuthUsuario> findByLogin(String login);

    boolean existsByLoginAndIdNot(String login, Long id);

}
