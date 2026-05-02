package br.com.projeto.piloto.aplicativos.application.port.in;



import java.util.List;
import java.util.Optional;

import br.com.projeto.piloto.aplicativos.domain.model.AplicativosModel;

public interface AplicativosUseCase {
    List<AplicativosModel> listAll();
    Optional<AplicativosModel> findById(Long id);
    AplicativosModel create(AplicativosModel model);
    AplicativosModel update(Long id, AplicativosModel model);
    void delete(Long id);
    boolean existsByNome(String nome);
    
    List<AplicativosModel> listAtivos();

}
