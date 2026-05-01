package br.com.projeto.piloto.adapter.out.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.projeto.piloto.adapter.out.jpa.entity.AuthPermissao;

@Repository
public interface SpringAuthPermissaoRepository extends JpaRepository<AuthPermissao, Long> {

     
	boolean existsByNmPermissao(String nmPermissao);
}
