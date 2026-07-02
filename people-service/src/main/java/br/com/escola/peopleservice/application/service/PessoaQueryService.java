package br.com.escola.peopleservice.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.context.InternalRequestContext;
import br.com.escola.peopleservice.application.dto.PessoaCatalogoResponse;
import br.com.escola.peopleservice.application.dto.PessoaConsultaCadastralPageResponse;
import br.com.escola.peopleservice.application.dto.PessoaResumoResponse;
import br.com.escola.peopleservice.application.exception.PeopleServiceResourceNotFoundException;
import br.com.escola.peopleservice.application.port.in.PessoaQueryUseCase;
import br.com.escola.peopleservice.application.port.out.PessoaReadPort;

@Service
public class PessoaQueryService implements PessoaQueryUseCase {

    private final PessoaReadPort pessoaReadPort;

    public PessoaQueryService(PessoaReadPort pessoaReadPort) {
        this.pessoaReadPort = pessoaReadPort;
    }

    @Override
    public List<PessoaCatalogoResponse> listarTiposPessoa(String authorization, InternalRequestContext context) {
        return pessoaReadPort.listarTiposPessoa(authorization, context);
    }

    @Override
    public List<PessoaCatalogoResponse> listarTiposEndereco(String authorization, InternalRequestContext context) {
        return pessoaReadPort.listarTiposEndereco(authorization, context);
    }

    @Override
    public PessoaResumoResponse buscarPessoaPorId(String authorization, InternalRequestContext context, UUID pessoaId) {
        return pessoaReadPort.buscarPessoaPorId(authorization, context, pessoaId)
                .orElseThrow(() -> new PeopleServiceResourceNotFoundException("Pessoa nao encontrada"));
    }

    @Override
    public PessoaConsultaCadastralPageResponse consultarCadastro(
            String authorization,
            InternalRequestContext context,
            String nomeAluno,
            String cpfAluno,
            String nomeResponsavel,
            String cpfResponsavel,
            int page,
            int size) {
        return pessoaReadPort.consultarCadastro(
                authorization,
                context,
                nomeAluno,
                cpfAluno,
                nomeResponsavel,
                cpfResponsavel,
                page,
                size);
    }
}
