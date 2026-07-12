package br.com.escola.peopleservice.application.port.out;

import br.com.escola.peopleservice.application.dto.PessoaConsultaCadastralPageResponse;

public interface AlunoResponsavelPort {

    PessoaConsultaCadastralPageResponse consultarCadastro(
            String nomeAluno,
            String cpfAluno,
            String nomeResponsavel,
            String cpfResponsavel,
            int page,
            int size);
}
