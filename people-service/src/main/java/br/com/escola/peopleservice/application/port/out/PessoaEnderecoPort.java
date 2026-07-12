package br.com.escola.peopleservice.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.escola.peopleservice.application.dto.PessoaEnderecoResponse;

public interface PessoaEnderecoPort {

    Optional<PessoaEnderecoResponse> buscarEnderecoPrincipalPorPessoa(UUID pessoaId, UUID escolaId);

    List<PessoaEnderecoResponse> listarEnderecosPorPessoa(UUID pessoaId, UUID escolaId);
}
