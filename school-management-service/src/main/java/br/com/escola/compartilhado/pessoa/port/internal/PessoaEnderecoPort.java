package br.com.escola.compartilhado.pessoa.port.internal;

import java.util.Optional;
import java.util.UUID;

import br.com.escola.compartilhado.pessoa.dto.internal.PessoaEnderecoResumo;

public interface PessoaEnderecoPort {

    Optional<PessoaEnderecoResumo> buscarEnderecoPrincipalPorPessoa(UUID pessoaId);

    void removerEnderecosDaPessoaRemovendoOrfaos(UUID pessoaId);
}
