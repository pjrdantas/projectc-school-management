package br.com.escola.seguranca.adapter.out.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.seguranca.adapter.out.persistence.entity.UsuarioPerfilEntity;

public interface UsuarioPerfilJpaRepository extends JpaRepository<UsuarioPerfilEntity, UUID> {

    List<UsuarioPerfilEntity> findByUsuarioId(UUID usuarioId);
}
