package br.com.escola.peopleservice.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.escola.peopleservice.application.context.InternalRequestContext;
import br.com.escola.peopleservice.application.dto.PessoaCatalogoResponse;
import br.com.escola.peopleservice.application.dto.PessoaConsultaCadastralPageResponse;
import br.com.escola.peopleservice.application.dto.PessoaResumoResponse;

public interface PessoaReadPort {

    List<PessoaCatalogoResponse> listarTiposPessoa(String authorization, InternalRequestContext context);

    List<PessoaCatalogoResponse> listarTiposEndereco(String authorization, InternalRequestContext context);

    Optional<PessoaResumoResponse> buscarPessoaPorId(String authorization, InternalRequestContext context, UUID pessoaId);

    PessoaConsultaCadastralPageResponse consultarCadastro(
            String authorization,
            InternalRequestContext context,
            String nomeAluno,
            String cpfAluno,
            String nomeResponsavel,
            String cpfResponsavel,
            int page,
            int size);
}
