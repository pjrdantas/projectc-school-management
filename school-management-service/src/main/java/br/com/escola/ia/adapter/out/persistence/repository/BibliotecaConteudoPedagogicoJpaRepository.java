package br.com.escola.ia.adapter.out.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.ia.adapter.out.persistence.entity.BibliotecaConteudoPedagogicoEntity;

public interface BibliotecaConteudoPedagogicoJpaRepository extends JpaRepository<BibliotecaConteudoPedagogicoEntity, UUID> {

    List<BibliotecaConteudoPedagogicoEntity> findByProfessorId(UUID professorId);
}
