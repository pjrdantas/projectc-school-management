package br.com.projeto.piloto.accesscontrol.application.port.out;

import java.util.List;
import java.util.Optional;

import org.springframework.lang.NonNull;

import br.com.projeto.piloto.accesscontrol.domain.model.AuthPerfilModel;

public interface AuthPerfilRepositoryPort {

    AuthPerfilModel create(@NonNull AuthPerfilModel domain);
    Optional<AuthPerfilModel> findById(Long id);    
    Optional<AuthPerfilModel> findByNmPerfil(String nome);
    
    AuthPerfilModel update(@NonNull Long id, @NonNull AuthPerfilModel domain);

    List<AuthPerfilModel> listAll();
    void delete(@NonNull Long id);
    
    boolean existsByNmPerfil(String nmPerfil);
}
