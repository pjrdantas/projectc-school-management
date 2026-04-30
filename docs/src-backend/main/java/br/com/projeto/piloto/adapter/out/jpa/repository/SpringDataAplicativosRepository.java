package br.com.projeto.piloto.adapter.out.jpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.projeto.piloto.adapter.out.jpa.entity.Aplicativo;

@Repository
public interface SpringDataAplicativosRepository extends JpaRepository<Aplicativo, Long> {
	
	boolean existsByNmAplicativo(String nmAplicativo);
	List<Aplicativo> findByFlAtivo(String flAtivo);
}
