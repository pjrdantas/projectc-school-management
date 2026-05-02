package br.com.projeto.piloto.accesscontrol.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.projeto.piloto.accesscontrol.adapter.out.persistence.entity.AuthPermissao;

@Repository
public interface SpringAuthPermissaoRepository extends JpaRepository<AuthPermissao, Long> {

     
	boolean existsByNmPermissao(String nmPermissao);
}
