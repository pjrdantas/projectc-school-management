package br.com.escola.peopleservice.application.port.out;

import br.com.escola.peopleservice.application.dto.PessoaEnderecoCleanupCommand;
import br.com.escola.peopleservice.application.dto.PessoaEnderecoWriteCommand;
import br.com.escola.peopleservice.application.dto.PessoaEnderecoWriteResult;

public interface PeopleAddressWritePort {

    PessoaEnderecoWriteResult criarOuAtualizarEnderecoPrincipal(PessoaEnderecoWriteCommand command);

    PessoaEnderecoWriteResult removerEnderecosDaPessoa(PessoaEnderecoCleanupCommand command);
}
