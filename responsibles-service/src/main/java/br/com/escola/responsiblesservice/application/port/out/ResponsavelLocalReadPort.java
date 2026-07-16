package br.com.escola.responsiblesservice.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.escola.responsiblesservice.application.dto.ResponsavelReadModelResponse;

public interface ResponsavelLocalReadPort {

    Optional<List<ResponsavelReadModelResponse>> listarResponsaveis(
            UUID escolaId,
            String nome,
            String cpf);

    Optional<ResponsavelReadModelResponse> buscarResponsavelPorId(
            UUID responsavelId,
            UUID escolaId);
}
