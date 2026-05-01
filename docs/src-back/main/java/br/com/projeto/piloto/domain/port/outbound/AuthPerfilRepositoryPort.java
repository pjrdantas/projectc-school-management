package br.com.projeto.piloto.domain.port.outbound;

import java.util.List;
import java.util.Optional;

import org.springframework.lang.NonNull;

import br.com.projeto.piloto.domain.model.AuthPerfilModel;

public interface AuthPerfilRepositoryPort {

    AuthPerfilModel create(@NonNull AuthPerfilModel domain);
    Optional<AuthPerfilModel> findById(Long id);    
    Optional<AuthPerfilModel> findByNmPerfil(String nome);
    
    AuthPerfilModel update(@NonNull Long id, @NonNull AuthPerfilModel domain);

    List<AuthPerfilModel> listAll();
    void delete(@NonNull Long id);
    
    boolean existsByNmPerfil(String nmPerfil);
}
