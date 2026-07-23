package br.com.escola.peopleservice.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.escola.peopleservice.application.dto.PessoaConsultaCadastralPageResponse;
import br.com.escola.peopleservice.application.dto.PessoaResponsavelVinculadoResponse;

public interface AlunoResponsavelPort {

    PessoaConsultaCadastralPageResponse consultarCadastro(
            String nomeAluno,
            String cpfAluno,
            String nomeResponsavel,
            String cpfResponsavel,
            int page,
            int size);

    Optional<List<PessoaResponsavelVinculadoResponse>> listarResponsaveisPorAluno(UUID alunoId);
}
