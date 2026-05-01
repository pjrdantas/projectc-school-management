package br.com.projeto.piloto.domain.port.outbound;

import java.util.List;
import java.util.Optional;

import br.com.projeto.piloto.domain.model.AplicativosModel;

public interface AplicativosRepositoryPort {

	List<AplicativosModel> findAll();

	Optional<AplicativosModel> findById(Long id);

	AplicativosModel save(AplicativosModel APplicativos);

	void deleteById(Long id);

	boolean existsByNome(String nome);

	List<AplicativosModel> findByAtivo(String ativo);

}
