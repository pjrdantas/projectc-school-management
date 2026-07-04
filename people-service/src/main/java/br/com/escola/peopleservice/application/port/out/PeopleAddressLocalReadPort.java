package br.com.escola.peopleservice.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.escola.peopleservice.application.dto.PessoaEnderecoLocalReadResponse;

public interface PeopleAddressLocalReadPort {

    Optional<PessoaEnderecoLocalReadResponse> buscarEnderecoPrincipalPorPessoa(UUID pessoaId, UUID escolaId);

    List<PessoaEnderecoLocalReadResponse> listarEnderecosPorPessoa(UUID pessoaId, UUID escolaId);
}
