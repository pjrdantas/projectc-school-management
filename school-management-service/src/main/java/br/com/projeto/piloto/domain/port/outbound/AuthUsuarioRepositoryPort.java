package br.com.projeto.piloto.domain.port.outbound;

import java.util.List;
import java.util.Optional;

import br.com.projeto.piloto.adapter.out.jpa.entity.AuthUsuario;

public interface AuthUsuarioRepositoryPort {

    AuthUsuario save( AuthUsuario usuario);
    Optional<AuthUsuario> findById( Long id);
    Optional<AuthUsuario> findByLogin(String login);
    List<AuthUsuario> findAll();
    void deleteById( Long id);
    boolean existsByDsLoginAndIdNot(String login, Long id);
    boolean existsById(Long id); 
    
}
