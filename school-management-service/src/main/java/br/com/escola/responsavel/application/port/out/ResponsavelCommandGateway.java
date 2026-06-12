package br.com.escola.responsavel.application.port.out;

import java.util.UUID;

import br.com.escola.responsavel.application.dto.ResponsavelInput;
import br.com.escola.responsavel.application.dto.ResponsavelOutput;

public interface ResponsavelCommandGateway {

    boolean existsByCpf(String cpf, UUID escolaId);

    boolean existsByCpfAndIdNot(String cpf, UUID escolaId, UUID id);

    ResponsavelOutput save(ResponsavelInput input);

    ResponsavelOutput update(UUID id, ResponsavelInput input);

    void deleteById(UUID id);
}
